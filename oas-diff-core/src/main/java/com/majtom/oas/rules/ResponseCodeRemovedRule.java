package com.majtom.oas.rules;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regel: Erkennt entfernte Response-Codes (Breaking Change bei Success-Codes).
 */
@Component
public class ResponseCodeRemovedRule implements BreakingChangeRule {

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

            checkOperationResponses(path, "GET", oldPathItem.getGet(), newPathItem.getGet(), changes);
            checkOperationResponses(path, "POST", oldPathItem.getPost(), newPathItem.getPost(), changes);
            checkOperationResponses(path, "PUT", oldPathItem.getPut(), newPathItem.getPut(), changes);
            checkOperationResponses(path, "DELETE", oldPathItem.getDelete(), newPathItem.getDelete(), changes);
            checkOperationResponses(path, "PATCH", oldPathItem.getPatch(), newPathItem.getPatch(), changes);
        }

        return changes;
    }

    private void checkOperationResponses(String path, String method, Operation oldOp, Operation newOp, List<ApiChange> changes) {
        if (oldOp == null || newOp == null) {
            return;
        }

        if (oldOp.getResponses() == null || newOp.getResponses() == null) {
            return;
        }

        Set<String> oldCodes = oldOp.getResponses().keySet();
        Set<String> newCodes = newOp.getResponses().keySet();

        for (String code : oldCodes) {
            if (!newCodes.contains(code)) {
                boolean isSuccess = isSuccessCode(code);
                changes.add(ApiChange.builder()
                        .type(ChangeType.RESPONSE_CODE_REMOVED)
                        .severity(isSuccess ? ChangeSeverity.CRITICAL : ChangeSeverity.MINOR)
                        .path(path + " [" + method + "]")
                        .description("Response-Code entfernt: " + code)
                        .oldValue(code)
                        .newValue(null)
                        .isBreakingChange(isSuccess)
                        .build());
            }
        }
    }

    private boolean isSuccessCode(String code) {
        try {
            int statusCode = Integer.parseInt(code);
            return statusCode >= 200 && statusCode < 300;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getRuleName() {
        return "Response Code Removed Rule";
    }
}

