package com.majtom.oas.rules.info;

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
 * Regel: Erkennt hinzugefügte Response Content-Types (Non-Breaking).
 * Wenn zusätzliche Response-Formate verfügbar werden, ist das kein Breaking Change.
 */
@Component
public class ResponseContentTypeAddedRule implements BreakingChangeRule {

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

        for (Map.Entry<String, ApiResponse> entry : newOp.getResponses().entrySet()) {
            String statusCode = entry.getKey();
            ApiResponse newResponse = entry.getValue();
            ApiResponse oldResponse = oldOp.getResponses().get(statusCode);

            if (oldResponse == null) {
                continue;
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

        // Prüfe auf neue Content-Types
        for (String contentType : newContentTypes) {
            if (!oldContentTypes.contains(contentType)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.RESPONSE_CONTENT_TYPE_ADDED)
                        .severity(ChangeSeverity.INFO)
                        .path(path + " [" + method + "] Response: " + statusCode)
                        .description("Neuer Response Content-Type verfügbar: " + contentType)
                        .oldValue(String.join(", ", oldContentTypes))
                        .newValue(contentType)
                        .isBreakingChange(false)
                        .build());
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Response Content Type Added Rule";
    }
}

