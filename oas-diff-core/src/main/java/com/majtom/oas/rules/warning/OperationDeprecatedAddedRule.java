package com.majtom.oas.rules.warning;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import com.majtom.oas.rules.BreakingChangeRule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt wenn eine Operation als deprecated markiert wurde (Warnung).
 * Signalisiert zukünftige Breaking Changes - Clients sollten auf Alternative umsteigen.
 */
@Component
public class OperationDeprecatedAddedRule implements BreakingChangeRule {

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

            checkOperationDeprecation(path, "GET", oldPathItem.getGet(), newPathItem.getGet(), changes);
            checkOperationDeprecation(path, "POST", oldPathItem.getPost(), newPathItem.getPost(), changes);
            checkOperationDeprecation(path, "PUT", oldPathItem.getPut(), newPathItem.getPut(), changes);
            checkOperationDeprecation(path, "DELETE", oldPathItem.getDelete(), newPathItem.getDelete(), changes);
            checkOperationDeprecation(path, "PATCH", oldPathItem.getPatch(), newPathItem.getPatch(), changes);
            checkOperationDeprecation(path, "HEAD", oldPathItem.getHead(), newPathItem.getHead(), changes);
            checkOperationDeprecation(path, "OPTIONS", oldPathItem.getOptions(), newPathItem.getOptions(), changes);
        }

        return changes;
    }

    private void checkOperationDeprecation(String path, String method, Operation oldOp, Operation newOp,
                                          List<ApiChange> changes) {
        if (oldOp == null || newOp == null) {
            return;
        }

        boolean wasDeprecated = Boolean.TRUE.equals(oldOp.getDeprecated());
        boolean isDeprecated = Boolean.TRUE.equals(newOp.getDeprecated());

        // Operation wurde als deprecated markiert
        if (!wasDeprecated && isDeprecated) {
            changes.add(ApiChange.builder()
                    .type(ChangeType.OPERATION_DEPRECATED_ADDED)
                    .severity(ChangeSeverity.WARNING)
                    .path(path + " [" + method + "]")
                    .description("Operation als deprecated markiert (wird in Zukunft entfernt)")
                    .oldValue("nicht deprecated")
                    .newValue("deprecated: true")
                    .isBreakingChange(false)
                    .build());
        }
    }

    @Override
    public String getRuleName() {
        return "Operation Deprecated Added Rule";
    }
}

