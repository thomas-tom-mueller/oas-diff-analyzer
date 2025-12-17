package com.majtom.oas.rules.info;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import com.majtom.oas.rules.BreakingChangeRule;
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
 * Regel: Erkennt hinzugefügte Request Content-Types (Non-Breaking).
 * Wenn zusätzliche Content-Types akzeptiert werden, ist das kein Breaking Change.
 */
@Component
public class RequestContentTypeAddedRule implements BreakingChangeRule {

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

        // Prüfe auf neue Content-Types
        for (String contentType : newContentTypes) {
            if (!oldContentTypes.contains(contentType)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.REQUEST_CONTENT_TYPE_ADDED)
                        .severity(ChangeSeverity.INFO)
                        .path(path + " [" + method + "]")
                        .description("Neuer Request Content-Type akzeptiert: " + contentType)
                        .oldValue(String.join(", ", oldContentTypes))
                        .newValue(contentType)
                        .isBreakingChange(false)
                        .build());
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Request Content Type Added Rule";
    }
}

