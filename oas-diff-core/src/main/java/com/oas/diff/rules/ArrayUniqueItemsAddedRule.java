package com.oas.diff.rules;

import com.oas.diff.model.ApiChange;
import com.oas.diff.model.ChangeSeverity;
import com.oas.diff.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt hinzugefügte uniqueItems-Constraints für Arrays (Breaking Change).
 * Wenn uniqueItems: true hinzugefügt wird, können Arrays mit Duplikaten ungültig werden.
 */
@Component
public class ArrayUniqueItemsAddedRule implements BreakingChangeRule {

    @Override
    public List<ApiChange> evaluate(OpenAPI oldSpec, OpenAPI newSpec) {
        List<ApiChange> changes = new ArrayList<>();

        if (oldSpec.getComponents() == null || oldSpec.getComponents().getSchemas() == null ||
            newSpec.getComponents() == null || newSpec.getComponents().getSchemas() == null) {
            return changes;
        }

        Map<String, Schema> oldSchemas = oldSpec.getComponents().getSchemas();
        Map<String, Schema> newSchemas = newSpec.getComponents().getSchemas();

        for (Map.Entry<String, Schema> entry : newSchemas.entrySet()) {
            String schemaName = entry.getKey();
            Schema newSchema = entry.getValue();
            Schema oldSchema = oldSchemas.get(schemaName);

            if (oldSchema == null) {
                continue;
            }

            checkArrayConstraints(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    @SuppressWarnings("rawtypes")
    private void checkArrayConstraints(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
        Map<String, Schema> oldProperties = oldSchema.getProperties();
        Map<String, Schema> newProperties = newSchema.getProperties();

        if (oldProperties == null || newProperties == null) {
            return;
        }

        for (Map.Entry<String, Schema> entry : newProperties.entrySet()) {
            String propName = entry.getKey();
            Schema newPropSchema = entry.getValue();
            Schema oldPropSchema = oldProperties.get(propName);

            if (oldPropSchema != null && isArrayType(newPropSchema)) {
                Boolean oldUniqueItems = oldPropSchema.getUniqueItems();
                Boolean newUniqueItems = newPropSchema.getUniqueItems();

                // uniqueItems wurde auf true gesetzt
                if (!Boolean.TRUE.equals(oldUniqueItems) && Boolean.TRUE.equals(newUniqueItems)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.ARRAY_UNIQUE_ITEMS_ADDED)
                            .severity(ChangeSeverity.MAJOR)
                            .path("Schema: " + schemaName + "." + propName)
                            .description("uniqueItems-Constraint hinzugefügt (Duplikate nicht mehr erlaubt)")
                            .oldValue("Duplikate erlaubt")
                            .newValue("uniqueItems: true")
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private boolean isArrayType(Schema schema) {
        return "array".equals(schema.getType());
    }

    @Override
    public String getRuleName() {
        return "Array UniqueItems Added Rule";
    }
}

