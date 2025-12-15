package com.oas.diff.rules;

import com.oas.diff.model.ApiChange;
import com.oas.diff.model.ChangeSeverity;
import com.oas.diff.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regel: Erkennt entfernte Optionen aus oneOf-Schemas (Breaking Change).
 * Wenn eine Option entfernt wird, werden bestimmte Werte nicht mehr akzeptiert.
 */
@Component
public class OneOfOptionRemovedRule implements BreakingChangeRule {

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

            checkOneOfOptions(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    @SuppressWarnings("rawtypes")
    private void checkOneOfOptions(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
        List<Schema> oldOneOf = oldSchema.getOneOf();
        List<Schema> newOneOf = newSchema.getOneOf();

        if (oldOneOf == null || oldOneOf.isEmpty()) {
            return;
        }

        if (newOneOf == null || newOneOf.isEmpty()) {
            // Alle oneOf-Optionen entfernt
            changes.add(ApiChange.builder()
                    .type(ChangeType.ONE_OF_OPTION_REMOVED)
                    .severity(ChangeSeverity.CRITICAL)
                    .path("Schema: " + schemaName)
                    .description("Alle oneOf-Optionen entfernt")
                    .oldValue(oldOneOf.size() + " Optionen")
                    .newValue("keine Optionen")
                    .isBreakingChange(true)
                    .build());
            return;
        }

        // Sammle Schema-Referenzen
        Set<String> oldRefs = getSchemaReferences(oldOneOf);
        Set<String> newRefs = getSchemaReferences(newOneOf);

        // Prüfe auf entfernte Optionen
        for (String oldRef : oldRefs) {
            if (!newRefs.contains(oldRef)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.ONE_OF_OPTION_REMOVED)
                        .severity(ChangeSeverity.MAJOR)
                        .path("Schema: " + schemaName)
                        .description("oneOf-Option entfernt")
                        .oldValue(oldRef)
                        .newValue(null)
                        .isBreakingChange(true)
                        .build());
            }
        }

        // Prüfe ob Anzahl reduziert wurde (auch bei nicht-referenzierten Schemas)
        if (newOneOf.size() < oldOneOf.size()) {
            int removed = oldOneOf.size() - newOneOf.size();
            changes.add(ApiChange.builder()
                    .type(ChangeType.ONE_OF_OPTION_REMOVED)
                    .severity(ChangeSeverity.MAJOR)
                    .path("Schema: " + schemaName)
                    .description("oneOf-Optionen reduziert")
                    .oldValue(oldOneOf.size() + " Optionen")
                    .newValue(newOneOf.size() + " Optionen (" + removed + " entfernt)")
                    .isBreakingChange(true)
                    .build());
        }
    }

    @SuppressWarnings("rawtypes")
    private Set<String> getSchemaReferences(List<Schema> schemas) {
        Set<String> refs = new HashSet<>();
        for (Schema schema : schemas) {
            String ref = schema.get$ref();
            if (ref != null) {
                refs.add(ref);
            }
        }
        return refs;
    }

    @Override
    public String getRuleName() {
        return "OneOf Option Removed Rule";
    }
}

