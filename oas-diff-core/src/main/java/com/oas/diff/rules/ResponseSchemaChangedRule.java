package com.oas.diff.rules;

import com.oas.diff.model.ApiChange;
import com.oas.diff.model.ChangeSeverity;
import com.oas.diff.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt Änderungen am Response-Schema (Breaking Change).
 */
@Component
public class ResponseSchemaChangedRule implements BreakingChangeRule {

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

        for (Map.Entry<String, ApiResponse> entry : oldOp.getResponses().entrySet()) {
            String statusCode = entry.getKey();
            ApiResponse oldResponse = entry.getValue();
            ApiResponse newResponse = newOp.getResponses().get(statusCode);

            if (newResponse == null) {
                continue; // Wird von ResponseCodeRemovedRule behandelt
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

        for (Map.Entry<String, MediaType> entry : oldContent.entrySet()) {
            String mediaTypeStr = entry.getKey();
            MediaType oldMediaType = entry.getValue();
            MediaType newMediaType = newContent.get(mediaTypeStr);

            if (newMediaType == null) {
                continue;
            }

            Schema oldSchema = oldMediaType.getSchema();
            Schema newSchema = newMediaType.getSchema();

            if (oldSchema != null && newSchema != null) {
                String oldSchemaRef = getSchemaReference(oldSchema);
                String newSchemaRef = getSchemaReference(newSchema);

                if (!oldSchemaRef.equals(newSchemaRef)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.RESPONSE_SCHEMA_CHANGED)
                            .severity(ChangeSeverity.MAJOR)
                            .path(path + " [" + method + "] Response: " + statusCode)
                            .description("Response-Schema geändert für " + mediaTypeStr)
                            .oldValue(oldSchemaRef)
                            .newValue(newSchemaRef)
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    private String getSchemaReference(Schema schema) {
        if (schema.get$ref() != null) {
            return schema.get$ref();
        }
        if (schema.getType() != null) {
            return schema.getType() + (schema.getFormat() != null ? "(" + schema.getFormat() + ")" : "");
        }
        return "unknown";
    }

    @Override
    public String getRuleName() {
        return "Response Schema Changed Rule";
    }
}

