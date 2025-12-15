package com.majtom.oas.rules;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt Änderungen am Parameter-Typ (Breaking Change).
 */
@Component
public class ParameterTypeChangedRule implements BreakingChangeRule {

    @Override
    public List<ApiChange> evaluate(OpenAPI oldSpec, OpenAPI newSpec) {
        List<ApiChange> changes = new ArrayList<>();

        if (oldSpec.getPaths() == null || newSpec.getPaths() == null) {
            return changes;
        }

        for (Map.Entry<String, PathItem> entry : oldSpec.getPaths().entrySet()) {
            String path = entry.getKey();
            PathItem oldPathItem = entry.getValue();
            PathItem newPathItem = newSpec.getPaths().get(path);

            if (newPathItem == null) {
                continue;
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

        for (Parameter oldParam : oldParams) {
            Parameter newParam = newParams.stream()
                    .filter(p -> p.getName().equals(oldParam.getName()) && p.getIn().equals(oldParam.getIn()))
                    .findFirst()
                    .orElse(null);

            if (newParam != null) {
                String oldType = getParameterType(oldParam);
                String newType = getParameterType(newParam);

                if (!oldType.equals(newType)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PARAMETER_TYPE_CHANGED)
                            .severity(ChangeSeverity.MAJOR)
                            .path(path + " [" + method + "]")
                            .description("Parameter-Typ geändert: " + oldParam.getName())
                            .oldValue(oldType)
                            .newValue(newType)
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    private String getParameterType(Parameter param) {
        if (param.getSchema() != null) {
            String type = param.getSchema().getType();
            String format = param.getSchema().getFormat();
            return type + (format != null ? "(" + format + ")" : "");
        }
        return "unknown";
    }

    @Override
    public String getRuleName() {
        return "Parameter Type Changed Rule";
    }
}

