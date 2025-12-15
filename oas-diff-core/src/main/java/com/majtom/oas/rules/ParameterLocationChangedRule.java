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
 * Regel: Erkennt Änderungen der Parameter-Location (Breaking Change).
 * Z.B. Parameter wechselt von query zu header.
 */
@Component
public class ParameterLocationChangedRule implements BreakingChangeRule {

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
            // Suche Parameter mit gleichem Namen in neuer Version
            Parameter newParam = findParameterByName(newParams, oldParam.getName());

            if (newParam != null) {
                String oldIn = oldParam.getIn();
                String newIn = newParam.getIn();

                if (oldIn != null && newIn != null && !oldIn.equals(newIn)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.PARAMETER_LOCATION_CHANGED)
                            .severity(ChangeSeverity.CRITICAL)
                            .path(path + " [" + method + "]")
                            .description("Parameter Location geändert: " + oldParam.getName())
                            .oldValue(oldIn)
                            .newValue(newIn)
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    private Parameter findParameterByName(List<Parameter> parameters, String name) {
        for (Parameter param : parameters) {
            if (param.getName().equals(name)) {
                return param;
            }
        }
        return null;
    }

    @Override
    public String getRuleName() {
        return "Parameter Location Changed Rule";
    }
}

