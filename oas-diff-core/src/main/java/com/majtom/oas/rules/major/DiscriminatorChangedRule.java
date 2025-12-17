package com.majtom.oas.rules.major;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import com.majtom.oas.rules.BreakingChangeRule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt Änderungen am Discriminator für polymorphe Schemas (Breaking Change).
 * Wenn der Discriminator Property Name geändert wird, bricht die Deserialisierung.
 */
@Component
public class DiscriminatorChangedRule implements BreakingChangeRule {

    @Override
    public List<ApiChange> evaluate(OpenAPI oldSpec, OpenAPI newSpec) {
        List<ApiChange> changes = new ArrayList<>();

        if (oldSpec.getComponents() == null || oldSpec.getComponents().getSchemas() == null ||
            newSpec.getComponents() == null || newSpec.getComponents().getSchemas() == null) {
            return changes;
        }

        Map<String, Schema> oldSchemas = oldSpec.getComponents().getSchemas();
        Map<String, Schema> newSchemas = newSpec.getComponents().getSchemas();

        for (Map.Entry<String, Schema> entry : oldSchemas.entrySet()) {
            String schemaName = entry.getKey();
            Schema oldSchema = entry.getValue();
            Schema newSchema = newSchemas.get(schemaName);

            if (newSchema == null) {
                continue;
            }

            checkDiscriminator(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    @SuppressWarnings("rawtypes")
    private void checkDiscriminator(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
        Discriminator oldDiscriminator = oldSchema.getDiscriminator();
        Discriminator newDiscriminator = newSchema.getDiscriminator();

        // Discriminator wurde hinzugefügt
        if (oldDiscriminator == null && newDiscriminator != null) {
            changes.add(ApiChange.builder()
                    .type(ChangeType.DISCRIMINATOR_CHANGED)
                    .severity(ChangeSeverity.MAJOR)
                    .path("Schema: " + schemaName)
                    .description("Discriminator hinzugefügt (Polymorphe Struktur geändert)")
                    .oldValue("kein Discriminator")
                    .newValue("propertyName: " + newDiscriminator.getPropertyName())
                    .isBreakingChange(true)
                    .build());
            return;
        }

        // Discriminator Property Name wurde geändert
        if (oldDiscriminator != null && newDiscriminator != null) {
            String oldPropertyName = oldDiscriminator.getPropertyName();
            String newPropertyName = newDiscriminator.getPropertyName();

            if (oldPropertyName != null && newPropertyName != null && !oldPropertyName.equals(newPropertyName)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.DISCRIMINATOR_CHANGED)
                        .severity(ChangeSeverity.CRITICAL)
                        .path("Schema: " + schemaName)
                        .description("Discriminator Property Name geändert")
                        .oldValue(oldPropertyName)
                        .newValue(newPropertyName)
                        .isBreakingChange(true)
                        .build());
            }
        }

        // Discriminator wurde entfernt
        if (oldDiscriminator != null && newDiscriminator == null) {
            changes.add(ApiChange.builder()
                    .type(ChangeType.DISCRIMINATOR_CHANGED)
                    .severity(ChangeSeverity.MAJOR)
                    .path("Schema: " + schemaName)
                    .description("Discriminator entfernt")
                    .oldValue("propertyName: " + oldDiscriminator.getPropertyName())
                    .newValue("kein Discriminator")
                    .isBreakingChange(true)
                    .build());
        }
    }

    @Override
    public String getRuleName() {
        return "Discriminator Changed Rule";
    }
}

