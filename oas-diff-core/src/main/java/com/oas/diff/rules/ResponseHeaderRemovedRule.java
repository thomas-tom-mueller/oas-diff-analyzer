package com.oas.diff.rules;

import com.oas.diff.model.ApiChange;
import com.oas.diff.model.ChangeSeverity;
import com.oas.diff.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regel: Erkennt entfernte Response-Headers (Breaking Change).
 * Wenn ein Response-Header entfernt wird, können Clients die darauf angewiesen sind, brechen.
 */
@Component
public class ResponseHeaderRemovedRule implements BreakingChangeRule {

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

            checkOperationHeaders(path, "GET", oldPathItem.getGet(), newPathItem.getGet(), changes);
            checkOperationHeaders(path, "POST", oldPathItem.getPost(), newPathItem.getPost(), changes);
            checkOperationHeaders(path, "PUT", oldPathItem.getPut(), newPathItem.getPut(), changes);
            checkOperationHeaders(path, "DELETE", oldPathItem.getDelete(), newPathItem.getDelete(), changes);
            checkOperationHeaders(path, "PATCH", oldPathItem.getPatch(), newPathItem.getPatch(), changes);
        }

        return changes;
    }

    private void checkOperationHeaders(String path, String method, Operation oldOp, Operation newOp,
                                      List<ApiChange> changes) {
        if (oldOp == null || newOp == null) {
            return;
        }

        if (oldOp.getResponses() == null || newOp.getResponses() == null) {
            return;
        }

        for (Map.Entry<String, ApiResponse> entry : oldOp.getResponses().entrySet()) {
            String statusCode = entry.getKey();
            ApiResponse oldResponse = entry.getValue();
            ApiResponse newResponse = newOp.getResponses().get(statusCode);

            if (newResponse == null) {
                continue;
            }

            checkResponseHeaders(path, method, statusCode, oldResponse, newResponse, changes);
        }
    }

    private void checkResponseHeaders(String path, String method, String statusCode,
                                      ApiResponse oldResponse, ApiResponse newResponse,
                                      List<ApiChange> changes) {
        Map<String, Header> oldHeaders = oldResponse.getHeaders();
        Map<String, Header> newHeaders = newResponse.getHeaders();

        if (oldHeaders == null || oldHeaders.isEmpty()) {
            return;
        }

        Set<String> oldHeaderNames = oldHeaders.keySet();

        if (newHeaders == null) {
            // Alle Header entfernt
            for (String headerName : oldHeaderNames) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.RESPONSE_HEADER_REMOVED)
                        .severity(ChangeSeverity.MAJOR)
                        .path(path + " [" + method + "] Response: " + statusCode)
                        .description("Response-Header entfernt: " + headerName)
                        .oldValue(headerName)
                        .newValue(null)
                        .isBreakingChange(true)
                        .build());
            }
            return;
        }

        Set<String> newHeaderNames = newHeaders.keySet();

        // Prüfe auf entfernte Header
        for (String headerName : oldHeaderNames) {
            if (!newHeaderNames.contains(headerName)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.RESPONSE_HEADER_REMOVED)
                        .severity(ChangeSeverity.MAJOR)
                        .path(path + " [" + method + "] Response: " + statusCode)
                        .description("Response-Header entfernt: " + headerName)
                        .oldValue(headerName)
                        .newValue(null)
                        .isBreakingChange(true)
                        .build());
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Response Header Removed Rule";
    }
}

