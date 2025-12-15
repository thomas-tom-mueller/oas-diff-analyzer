package com.oas.diff.rules;

import com.oas.diff.model.ApiChange;
import com.oas.diff.model.ChangeSeverity;
import com.oas.diff.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt Änderungen am Parameter-explode Flag (Breaking Change).
 * Ändert die Array/Object-Serialisierung.
 */
@Component
public class ParameterExplodeChangedRule implements BreakingChangeRule {

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

    private void checkOperationParameters(String path, String method, Operation oldOp, Operation newOp,
                                         List<ApiChange> changes) {
        if (oldOp == null || newOp == null) {
            return;
        }

        List<Parameter> oldParams = oldOp.getParameters() != null ? oldOp.getParameters() : new ArrayList<>();
        List<Parameter> newParams = newOp.getParameters() != null ? newOp.getParameters() : new ArrayList<>();

        for (Parameter oldParam : oldParams) {
            Parameter newParam = findParameter(newParams, oldParam.getName(), oldParam.getIn());

            if (newParam != null) {
                Boolean oldExplode = oldParam.getExplode();
                Boolean newExplode = newParam.getExplode();

                // Default-Wert beachten (explode ist true für form-style per Default)
                boolean oldValue = oldExplode != null ? oldExplode : isDefaultExplodeTrue(oldParam);
                boolean newValue = newExplode != null ? newExplode : isDefaultExplodeTrue(newParam);

                if (oldValue != newValue) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PARAMETER_EXPLODE_CHANGED)
                            .severity(ChangeSeverity.MAJOR)
                            .path(path + " [" + method + "]")
                            .description("Parameter explode-Flag geändert: " + oldParam.getName())
                            .oldValue(String.valueOf(oldValue))
                            .newValue(String.valueOf(newValue))
                            .isBreakingChange(true)
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

    private boolean isDefaultExplodeTrue(Parameter param) {
        // explode ist true per default für form-style
        Parameter.StyleEnum style = param.getStyle();
        return style == null || style.equals(Parameter.StyleEnum.FORM);
    }

    @Override
    public String getRuleName() {
        return "Parameter Explode Changed Rule";
    }
}

