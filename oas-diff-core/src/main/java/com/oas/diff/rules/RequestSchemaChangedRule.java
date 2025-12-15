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
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt Änderungen am Request-Schema (Breaking Change).
 */
@Component
public class RequestSchemaChangedRule implements BreakingChangeRule {

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

            checkOperationRequestBody(path, "POST", oldPathItem.getPost(), newPathItem.getPost(), changes);
            checkOperationRequestBody(path, "PUT", oldPathItem.getPut(), newPathItem.getPut(), changes);
            checkOperationRequestBody(path, "PATCH", oldPathItem.getPatch(), newPathItem.getPatch(), changes);
        }

        return changes;
    }

    private void checkOperationRequestBody(String path, String method, Operation oldOp, Operation newOp, List<ApiChange> changes) {
        if (oldOp == null || newOp == null) {
            return;
        }

        RequestBody oldRequestBody = oldOp.getRequestBody();
        RequestBody newRequestBody = newOp.getRequestBody();

        if (oldRequestBody == null || newRequestBody == null) {
            return;
        }

        Content oldContent = oldRequestBody.getContent();
        Content newContent = newRequestBody.getContent();

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
                            .type(ChangeType.REQUEST_SCHEMA_CHANGED)
                            .severity(ChangeSeverity.MAJOR)
                            .path(path + " [" + method + "]")
                            .description("Request-Schema geändert für " + mediaTypeStr)
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
        return "Request Schema Changed Rule";
    }
}

