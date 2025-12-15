package com.majtom.oas.rules;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regel: Erkennt entfernte Request Content-Types (Breaking Change).
 * Wenn ein Content-Type nicht mehr akzeptiert wird (z.B. kein XML mehr), ist das ein Breaking Change.
 */
@Component
public class RequestContentTypeRemovedRule implements BreakingChangeRule {

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

            checkOperationRequestContent(path, "POST", oldPathItem.getPost(), newPathItem.getPost(), changes);
            checkOperationRequestContent(path, "PUT", oldPathItem.getPut(), newPathItem.getPut(), changes);
            checkOperationRequestContent(path, "PATCH", oldPathItem.getPatch(), newPathItem.getPatch(), changes);
        }

        return changes;
    }

    private void checkOperationRequestContent(String path, String method, Operation oldOp, Operation newOp,
                                             List<ApiChange> changes) {
        if (oldOp == null || newOp == null) {
            return;
        }

        RequestBody oldBody = oldOp.getRequestBody();
        RequestBody newBody = newOp.getRequestBody();

        if (oldBody == null || newBody == null) {
            return;
        }

        Content oldContent = oldBody.getContent();
        Content newContent = newBody.getContent();

        if (oldContent == null || newContent == null) {
            return;
        }

        Set<String> oldContentTypes = oldContent.keySet();
        Set<String> newContentTypes = newContent.keySet();

        // Prüfe auf entfernte Content-Types
        for (String contentType : oldContentTypes) {
            if (!newContentTypes.contains(contentType)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.REQUEST_CONTENT_TYPE_REMOVED)
                        .severity(ChangeSeverity.CRITICAL)
                        .path(path + " [" + method + "]")
                        .description("Request Content-Type nicht mehr unterstützt: " + contentType)
                        .oldValue(contentType)
                        .newValue(null)
                        .isBreakingChange(true)
                        .build());
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Request Content Type Removed Rule";
    }
}

