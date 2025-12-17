package com.majtom.oas.rules.critical;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import com.majtom.oas.rules.BreakingChangeRule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt neue Required-Parameter (Breaking Change).
 */
@Component
public class RequiredParameterAddedRule implements BreakingChangeRule {

    @Override
    public List<ApiChange> evaluate(OpenAPI oldSpec, OpenAPI newSpec) {
        List<ApiChange> changes = new ArrayList<>();

        if (oldSpec.getPaths() == null || newSpec.getPaths() == null) {
            return changes;
        }

        for (Map.Entry<String, PathItem> entry : newSpec.getPaths().entrySet()) {
            String path = entry.getKey();
            PathItem newPathItem = entry.getValue();
            PathItem oldPathItem = oldSpec.getPaths().get(path);

            if (oldPathItem == null) {
                continue; // Neuer Endpoint, kein Breaking Change
            }

            checkOperationParameters(path, "GET", oldPathItem.getGet(), newPathItem.getGet(), changes);
            checkOperationParameters(path, "POST", oldPathItem.getPost(), newPathItem.getPost(), changes);
            checkOperationParameters(path, "PUT", oldPathItem.getPut(), newPathItem.getPut(), changes);
            checkOperationParameters(path, "DELETE", oldPathItem.getDelete(), newPathItem.getDelete(), changes);
            checkOperationParameters(path, "PATCH", oldPathItem.getPatch(), newPathItem.getPatch(), changes);
        }

        return changes;
    }

    private void checkOperationParameters(String path, String method, Operation oldOp, Operation newOp, List<ApiChange> changes) {
        if (oldOp == null || newOp == null) {
            return;
        }

        List<Parameter> oldParams = oldOp.getParameters() != null ? oldOp.getParameters() : new ArrayList<>();
        List<Parameter> newParams = newOp.getParameters() != null ? newOp.getParameters() : new ArrayList<>();

        for (Parameter newParam : newParams) {
            if (Boolean.TRUE.equals(newParam.getRequired())) {
                boolean existedBefore = oldParams.stream()
                        .anyMatch(p -> p.getName().equals(newParam.getName()) && p.getIn().equals(newParam.getIn()));

                if (!existedBefore) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PARAMETER_REQUIRED_ADDED)
                            .severity(ChangeSeverity.CRITICAL)
                            .path(path + " [" + method + "]")
                            .description("Neuer Required-Parameter hinzugefügt: " + newParam.getName() + " (" + newParam.getIn() + ")")
                            .oldValue(null)
                            .newValue(newParam.getName())
                            .isBreakingChange(true)
                            .build());
                }
            }
        }

        // Parameter wurde von optional zu required
        for (Parameter oldParam : oldParams) {
            if (Boolean.FALSE.equals(oldParam.getRequired()) || oldParam.getRequired() == null) {
                Parameter newParam = newParams.stream()
                        .filter(p -> p.getName().equals(oldParam.getName()) && p.getIn().equals(oldParam.getIn()))
                        .findFirst()
                        .orElse(null);

                if (newParam != null && Boolean.TRUE.equals(newParam.getRequired())) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PARAMETER_REQUIRED_ADDED)
                            .severity(ChangeSeverity.MAJOR)
                            .path(path + " [" + method + "]")
                            .description("Parameter wurde zu Required: " + newParam.getName() + " (" + newParam.getIn() + ")")
                            .oldValue("optional")
                            .newValue("required")
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Required Parameter Added Rule";
    }
}

