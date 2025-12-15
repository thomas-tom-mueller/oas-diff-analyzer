package com.majtom.oas.rules;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt wenn Request Body zu required wird (Breaking Change).
 */
@Component
public class RequestBodyRequiredAddedRule implements BreakingChangeRule {

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

            checkOperationRequestBody(path, "POST", oldPathItem.getPost(), newPathItem.getPost(), changes);
            checkOperationRequestBody(path, "PUT", oldPathItem.getPut(), newPathItem.getPut(), changes);
            checkOperationRequestBody(path, "PATCH", oldPathItem.getPatch(), newPathItem.getPatch(), changes);
        }

        return changes;
    }

    private void checkOperationRequestBody(String path, String method, Operation oldOp, Operation newOp,
                                          List<ApiChange> changes) {
        if (oldOp == null || newOp == null) {
            return;
        }

        RequestBody oldBody = oldOp.getRequestBody();
        RequestBody newBody = newOp.getRequestBody();

        if (oldBody == null && newBody != null && Boolean.TRUE.equals(newBody.getRequired())) {
            changes.add(ApiChange.builder()
                    .type(ChangeType.REQUEST_BODY_REQUIRED_ADDED)
                    .severity(ChangeSeverity.CRITICAL)
                    .path(path + " [" + method + "]")
                    .description("Request-Body hinzugefügt und als required markiert")
                    .oldValue("kein Body")
                    .newValue("required Body")
                    .isBreakingChange(true)
                    .build());
            return;
        }

        if (oldBody != null && newBody != null) {
            boolean wasRequired = Boolean.TRUE.equals(oldBody.getRequired());
            boolean isRequired = Boolean.TRUE.equals(newBody.getRequired());

            if (!wasRequired && isRequired) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.REQUEST_BODY_REQUIRED_ADDED)
                        .severity(ChangeSeverity.MAJOR)
                        .path(path + " [" + method + "]")
                        .description("Request-Body wurde von optional zu required geändert")
                        .oldValue("optional")
                        .newValue("required")
                        .isBreakingChange(true)
                        .build());
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Request Body Required Added Rule";
    }
}

