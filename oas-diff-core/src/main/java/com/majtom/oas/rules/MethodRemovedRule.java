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

/**
 * Regel: Erkennt entfernte HTTP-Methoden auf bestehenden Endpoints (Breaking Change).
 */
@Component
public class MethodRemovedRule implements BreakingChangeRule {

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
                continue; // Wird von EndpointRemovedRule behandelt
            }

            checkMethod(path, "GET", oldPathItem.getGet(), newPathItem.getGet(), changes);
            checkMethod(path, "POST", oldPathItem.getPost(), newPathItem.getPost(), changes);
            checkMethod(path, "PUT", oldPathItem.getPut(), newPathItem.getPut(), changes);
            checkMethod(path, "DELETE", oldPathItem.getDelete(), newPathItem.getDelete(), changes);
            checkMethod(path, "PATCH", oldPathItem.getPatch(), newPathItem.getPatch(), changes);
            checkMethod(path, "HEAD", oldPathItem.getHead(), newPathItem.getHead(), changes);
            checkMethod(path, "OPTIONS", oldPathItem.getOptions(), newPathItem.getOptions(), changes);
        }

        return changes;
    }

    private void checkMethod(String path, String method, Operation oldOp, Operation newOp, List<ApiChange> changes) {
        if (oldOp != null && newOp == null) {
            changes.add(ApiChange.builder()
                    .type(ChangeType.METHOD_REMOVED)
                    .severity(ChangeSeverity.CRITICAL)
                    .path(path + " [" + method + "]")
                    .description("HTTP-Methode wurde entfernt")
                    .oldValue(method)
                    .newValue(null)
                    .isBreakingChange(true)
                    .build());
        }
    }

    @Override
    public String getRuleName() {
        return "Method Removed Rule";
    }
}

