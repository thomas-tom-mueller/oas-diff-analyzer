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
 * Regel: Erkennt Änderungen am Parameter-Serialisierungs-Style (Breaking Change).
 * Z.B. style: form → style: deepObject
 */
@Component
public class ParameterStyleChangedRule implements BreakingChangeRule {

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
                Parameter.StyleEnum oldStyle = oldParam.getStyle();
                Parameter.StyleEnum newStyle = newParam.getStyle();

                if (oldStyle != null && newStyle != null && !oldStyle.equals(newStyle)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PARAMETER_STYLE_CHANGED)
                            .severity(ChangeSeverity.MAJOR)
                            .path(path + " [" + method + "]")
                            .description("Parameter Serialisierungs-Style geändert: " + oldParam.getName())
                            .oldValue(oldStyle.toString())
                            .newValue(newStyle.toString())
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

    @Override
    public String getRuleName() {
        return "Parameter Style Changed Rule";
    }
}

