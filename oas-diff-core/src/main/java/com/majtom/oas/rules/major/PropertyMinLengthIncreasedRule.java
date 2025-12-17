package com.majtom.oas.rules.major;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import com.majtom.oas.rules.BreakingChangeRule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt erhöhte minLength-Constraints (Breaking Change).
 * Wenn minLength erhöht wird, können kürzere Werte ungültig werden.
 */
@Component
public class PropertyMinLengthIncreasedRule implements BreakingChangeRule {

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

            checkMinLengthConstraints(schemaName, oldSchema, newSchema, changes);
        }

        return changes;
    }

    @SuppressWarnings("rawtypes")
    private void checkMinLengthConstraints(String schemaName, Schema oldSchema, Schema newSchema, List<ApiChange> changes) {
        Map<String, Schema> oldProperties = oldSchema.getProperties();
        Map<String, Schema> newProperties = newSchema.getProperties();

        if (oldProperties == null || newProperties == null) {
            return;
        }

        for (Map.Entry<String, Schema> entry : oldProperties.entrySet()) {
            String propName = entry.getKey();
            Schema oldPropSchema = entry.getValue();
            Schema newPropSchema = newProperties.get(propName);

            if (newPropSchema != null && isStringType(oldPropSchema)) {
                Integer oldMinLength = oldPropSchema.getMinLength();
                Integer newMinLength = newPropSchema.getMinLength();

                // minLength wurde erhöht
                if (hasIncreased(oldMinLength, newMinLength)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PROPERTY_MIN_LENGTH_INCREASED)
                            .severity(ChangeSeverity.MAJOR)
                            .path("Schema: " + schemaName + "." + propName)
                            .description("minLength erhöht (Validierung verschärft)")
                            .oldValue(String.valueOf(oldMinLength != null ? oldMinLength : 0))
                            .newValue(String.valueOf(newMinLength))
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    private boolean hasIncreased(Integer oldValue, Integer newValue) {
        if (newValue == null) {
            return false; // Constraint entfernt = weniger restriktiv
        }
        int oldVal = oldValue != null ? oldValue : 0;
        return newValue > oldVal;
    }

    @SuppressWarnings("rawtypes")
    private boolean isStringType(Schema schema) {
        return "string".equals(schema.getType());
    }

    @Override
    public String getRuleName() {
        return "Property MinLength Increased Rule";
    }
}

