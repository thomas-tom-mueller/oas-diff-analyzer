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
 * Regel: Erkennt entfernte Request Bodies (Breaking Change).
 * Wenn eine Operation keinen Request Body mehr akzeptiert, ist das ein kritischer Breaking Change.
 */
@Component
public class RequestBodyRemovedRule implements BreakingChangeRule {

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
                continue; // Endpoint entfernt, wird von EndpointRemovedRule behandelt
            }

            checkOperationRequestBody(path, "POST", oldPathItem.getPost(), newPathItem.getPost(), changes);
            checkOperationRequestBody(path, "PUT", oldPathItem.getPut(), newPathItem.getPut(), changes);
            checkOperationRequestBody(path, "PATCH", oldPathItem.getPatch(), newPathItem.getPatch(), changes);
            checkOperationRequestBody(path, "DELETE", oldPathItem.getDelete(), newPathItem.getDelete(), changes);
        }

        return changes;
    }

    private void checkOperationRequestBody(String path, String method, Operation oldOp, Operation newOp,
                                          List<ApiChange> changes) {
        if (oldOp == null || newOp == null) {
            return;
        }

        boolean oldHasBody = oldOp.getRequestBody() != null;
        boolean newHasBody = newOp.getRequestBody() != null;

        if (oldHasBody && !newHasBody) {
            changes.add(ApiChange.builder()
                    .type(ChangeType.REQUEST_BODY_REMOVED)
                    .severity(ChangeSeverity.CRITICAL)
                    .path(path + " [" + method + "]")
                    .description("Request-Body wurde entfernt")
                    .oldValue("vorhanden")
                    .newValue("entfernt")
                    .isBreakingChange(true)
                    .build());
        }
    }

    @Override
    public String getRuleName() {
        return "Request Body Removed Rule";
    }
}

