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
 * Regel: Erkennt geänderte Callback-URLs (Breaking Change).
 * Wenn Callback URL Patterns geändert werden, ist das ein Breaking Change.
 */
@Component
public class CallbackUrlChangedRule implements BreakingChangeRule {

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

        if (oldCallbacks == null || oldCallbacks.isEmpty() ||
            newCallbacks == null || newCallbacks.isEmpty()) {
            return;
        }

        // Prüfe auf geänderte Callback-URLs
        for (Map.Entry<String, Callback> entry : oldCallbacks.entrySet()) {
            String callbackName = entry.getKey();
            Callback oldCallback = entry.getValue();
            Callback newCallback = newCallbacks.get(callbackName);

            if (newCallback != null) {
                // Vergleiche die URLs in den Callbacks
                String oldUrls = getCallbackUrls(oldCallback);
                String newUrls = getCallbackUrls(newCallback);

                if (!oldUrls.equals(newUrls)) {
                    changes.add(ApiChange.builder()
                            .type(ChangeType.CALLBACK_URL_CHANGED)
                            .severity(ChangeSeverity.MINOR)
                            .path(path + " [" + method + "]")
                            .description("Callback-URL geändert: " + callbackName)
                            .oldValue(oldUrls)
                            .newValue(newUrls)
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    private String getCallbackUrls(Callback callback) {
        if (callback == null) {
            return "";
        }
        // Sammle alle URL-Patterns aus dem Callback
        List<String> urls = new ArrayList<>(callback.keySet());
        return String.join(", ", urls);
    }

    @Override
    public String getRuleName() {
        return "Callback URL Changed Rule";
    }
}

