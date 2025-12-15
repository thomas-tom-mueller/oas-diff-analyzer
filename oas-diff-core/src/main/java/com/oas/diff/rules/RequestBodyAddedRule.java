package com.oas.diff.rules;

import com.oas.diff.model.ApiChange;
import com.oas.diff.model.ChangeSeverity;
import com.oas.diff.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt hinzugefügte optionale Request Bodies (Non-Breaking).
 * Wenn ein optionaler Request Body hinzugefügt wird, ist das kein Breaking Change.
 */
@Component
public class RequestBodyAddedRule implements BreakingChangeRule {

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
            checkOperationRequestBody(path, "DELETE", oldPathItem.getDelete(), newPathItem.getDelete(), changes);
        }

        return changes;
    }

    private void checkOperationRequestBody(String path, String method, Operation oldOp, Operation newOp,
                                          List<ApiChange> changes) {
        if (oldOp == null || newOp == null) {
            return;
        }

        boolean oldHasBody = oldOp.getRequestBody() != null;
        boolean newHasBody = newOp.getRequestBody() != null;

        // Request Body wurde hinzugefügt
        if (!oldHasBody && newHasBody) {
            boolean isRequired = Boolean.TRUE.equals(newOp.getRequestBody().getRequired());

            // Nur melden wenn optional (required wird von RequestBodyRequiredAddedRule behandelt)
            if (!isRequired) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.REQUEST_BODY_ADDED)
                        .severity(ChangeSeverity.INFO)
                        .path(path + " [" + method + "]")
                        .description("Optionaler Request-Body hinzugefügt")
                        .oldValue("kein Body")
                        .newValue("optionaler Body")
                        .isBreakingChange(false)
                        .build());
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Request Body Added Rule";
    }
}

