package com.majtom.oas.rules;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.callbacks.Callback;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt hinzugefügte Callbacks (Non-Breaking).
 * Neue Callbacks erweitern die API-Funktionalität ohne Breaking Changes.
 */
@Component
public class CallbackAddedRule implements BreakingChangeRule {

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

            checkOperationCallbacks(path, "POST", oldPathItem.getPost(), newPathItem.getPost(), changes);
            checkOperationCallbacks(path, "PUT", oldPathItem.getPut(), newPathItem.getPut(), changes);
            checkOperationCallbacks(path, "PATCH", oldPathItem.getPatch(), newPathItem.getPatch(), changes);
        }

        return changes;
    }

    private void checkOperationCallbacks(String path, String method, Operation oldOp, Operation newOp,
                                        List<ApiChange> changes) {
        if (oldOp == null || newOp == null) {
            return;
        }

        Map<String, Callback> oldCallbacks = oldOp.getCallbacks();
        Map<String, Callback> newCallbacks = newOp.getCallbacks();

        if (newCallbacks == null || newCallbacks.isEmpty()) {
            return;
        }

        if (oldCallbacks == null || oldCallbacks.isEmpty()) {
            // Alle Callbacks neu hinzugefügt
            for (String callbackName : newCallbacks.keySet()) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.CALLBACK_ADDED)
                        .severity(ChangeSeverity.INFO)
                        .path(path + " [" + method + "]")
                        .description("Callback hinzugefügt: " + callbackName)
                        .oldValue(null)
                        .newValue(callbackName)
                        .isBreakingChange(false)
                        .build());
            }
            return;
        }

        // Prüfe auf neue Callbacks
        for (String callbackName : newCallbacks.keySet()) {
            if (!oldCallbacks.containsKey(callbackName)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.CALLBACK_ADDED)
                        .severity(ChangeSeverity.INFO)
                        .path(path + " [" + method + "]")
                        .description("Callback hinzugefügt: " + callbackName)
                        .oldValue(null)
                        .newValue(callbackName)
                        .isBreakingChange(false)
                        .build());
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Callback Added Rule";
    }
}

