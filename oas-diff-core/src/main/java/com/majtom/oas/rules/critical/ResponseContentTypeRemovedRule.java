package com.majtom.oas.rules.critical;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import com.majtom.oas.rules.BreakingChangeRule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regel: Erkennt entfernte Response Content-Types (Breaking Change).
 * Wenn ein Response Content-Type nicht mehr geliefert wird, ist das ein Breaking Change.
 */
@Component
public class ResponseContentTypeRemovedRule implements BreakingChangeRule {

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

    private void checkOperationResponses(String path, String method, Operation oldOp, Operation newOp,
                                        List<ApiChange> changes) {
        if (oldOp == null || newOp == null) {
            return;
        }

        if (oldOp.getResponses() == null || newOp.getResponses() == null) {
            return;
        }

        // Prüfe nur Success-Responses (2xx)
        for (Map.Entry<String, ApiResponse> entry : oldOp.getResponses().entrySet()) {
            String statusCode = entry.getKey();

            if (!isSuccessCode(statusCode)) {
                continue;
            }

            ApiResponse oldResponse = entry.getValue();
            ApiResponse newResponse = newOp.getResponses().get(statusCode);

            if (newResponse == null) {
                continue; // Response entfernt, wird von ResponseCodeRemovedRule behandelt
            }

            checkResponseContent(path, method, statusCode, oldResponse, newResponse, changes);
        }
    }

    private void checkResponseContent(String path, String method, String statusCode,
                                      ApiResponse oldResponse, ApiResponse newResponse,
                                      List<ApiChange> changes) {
        Content oldContent = oldResponse.getContent();
        Content newContent = newResponse.getContent();

        if (oldContent == null || newContent == null) {
            return;
        }

        Set<String> oldContentTypes = oldContent.keySet();
        Set<String> newContentTypes = newContent.keySet();

        // Prüfe auf entfernte Content-Types
        for (String contentType : oldContentTypes) {
            if (!newContentTypes.contains(contentType)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.RESPONSE_CONTENT_TYPE_REMOVED)
                        .severity(ChangeSeverity.CRITICAL)
                        .path(path + " [" + method + "] Response: " + statusCode)
                        .description("Response Content-Type nicht mehr verfügbar: " + contentType)
                        .oldValue(contentType)
                        .newValue(null)
                        .isBreakingChange(true)
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
        return "Response Content Type Removed Rule";
    }
}

