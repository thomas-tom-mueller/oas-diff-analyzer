package com.majtom.oas.rules;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
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
 * Regel: Erkennt hinzugefügte Response-Headers (Non-Breaking).
 * Wenn neue Response-Headers hinzugefügt werden, ist das kein Breaking Change.
 */
@Component
public class ResponseHeaderAddedRule implements BreakingChangeRule {

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

        for (Map.Entry<String, ApiResponse> entry : newOp.getResponses().entrySet()) {
            String statusCode = entry.getKey();
            ApiResponse newResponse = entry.getValue();
            ApiResponse oldResponse = oldOp.getResponses().get(statusCode);

            if (oldResponse == null) {
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

        if (newHeaders == null || newHeaders.isEmpty()) {
            return;
        }

        Set<String> oldHeaderNames = oldHeaders != null ? oldHeaders.keySet() : Set.of();
        Set<String> newHeaderNames = newHeaders.keySet();

        // Prüfe auf neue Header
        for (String headerName : newHeaderNames) {
            if (!oldHeaderNames.contains(headerName)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.RESPONSE_HEADER_ADDED)
                        .severity(ChangeSeverity.INFO)
                        .path(path + " [" + method + "] Response: " + statusCode)
                        .description("Neuer Response-Header hinzugefügt: " + headerName)
                        .oldValue(null)
                        .newValue(headerName)
                        .isBreakingChange(false)
                        .build());
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Response Header Added Rule";
    }
}

