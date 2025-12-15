package com.oas.diff.rules;

import com.oas.diff.model.ApiChange;
import com.oas.diff.model.ChangeSeverity;
import com.oas.diff.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.callbacks.Callback;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt entfernte Callbacks (Breaking Change).
 * Wenn Callbacks entfernt werden, können Clients die darauf angewiesen sind, brechen.
 */
@Component
public class CallbackRemovedRule implements BreakingChangeRule {

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

        if (oldCallbacks == null || oldCallbacks.isEmpty()) {
            return;
        }

        if (newCallbacks == null || newCallbacks.isEmpty()) {
            // Alle Callbacks entfernt
            for (String callbackName : oldCallbacks.keySet()) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.CALLBACK_REMOVED)
                        .severity(ChangeSeverity.MINOR)
                        .path(path + " [" + method + "]")
                        .description("Callback entfernt: " + callbackName)
                        .oldValue(callbackName)
                        .newValue(null)
                        .isBreakingChange(true)
                        .build());
            }
            return;
        }

        // Prüfe auf entfernte Callbacks
        for (String callbackName : oldCallbacks.keySet()) {
            if (!newCallbacks.containsKey(callbackName)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.CALLBACK_REMOVED)
                        .severity(ChangeSeverity.MINOR)
                        .path(path + " [" + method + "]")
                        .description("Callback entfernt: " + callbackName)
                        .oldValue(callbackName)
                        .newValue(null)
                        .isBreakingChange(true)
                        .build());
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Callback Removed Rule";
    }
}

