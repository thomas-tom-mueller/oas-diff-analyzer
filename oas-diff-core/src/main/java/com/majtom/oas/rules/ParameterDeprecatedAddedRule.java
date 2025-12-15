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
 * Regel: Erkennt wenn ein Parameter als deprecated markiert wurde (Warnung).
 * Signalisiert zukünftige Breaking Changes - Clients sollten Parameter nicht mehr verwenden.
 */
@Component
public class ParameterDeprecatedAddedRule implements BreakingChangeRule {

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

    private void checkOperationParameters(String path, String method, Operation oldOp, Operation newOp,
                                         List<ApiChange> changes) {
        if (oldOp == null || newOp == null) {
            return;
        }

        List<Parameter> oldParams = oldOp.getParameters() != null ? oldOp.getParameters() : new ArrayList<>();
        List<Parameter> newParams = newOp.getParameters() != null ? newOp.getParameters() : new ArrayList<>();

        for (Parameter newParam : newParams) {
            Parameter oldParam = findParameter(oldParams, newParam.getName(), newParam.getIn());

            if (oldParam != null) {
                boolean wasDeprecated = Boolean.TRUE.equals(oldParam.getDeprecated());
                boolean isDeprecated = Boolean.TRUE.equals(newParam.getDeprecated());

                // Parameter wurde als deprecated markiert
                if (!wasDeprecated && isDeprecated) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PARAMETER_DEPRECATED_ADDED)
                            .severity(ChangeSeverity.WARNING)
                            .path(path + " [" + method + "]")
                            .description("Parameter als deprecated markiert: " + newParam.getName())
                            .oldValue("nicht deprecated")
                            .newValue("deprecated: true")
                            .isBreakingChange(false)
                            .build());
                }
            }
        }
    }

    private Parameter findParameter(List<Parameter> parameters, String name, String in) {
        for (Parameter param : parameters) {
            if (param.getName().equals(name) && param.getIn().equals(in)) {
                return param;
            }
        }
        return null;
    }

    @Override
    public String getRuleName() {
        return "Parameter Deprecated Added Rule";
    }
}

