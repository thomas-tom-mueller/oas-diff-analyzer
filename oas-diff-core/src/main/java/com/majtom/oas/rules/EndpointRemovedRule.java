package com.majtom.oas.rules;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Regel: Erkennt entfernte Endpoints (Breaking Change).
 */
@Component
public class EndpointRemovedRule implements BreakingChangeRule {

    @Override
    public List<ApiChange> evaluate(OpenAPI oldSpec, OpenAPI newSpec) {
        List<ApiChange> changes = new ArrayList<>();

        if (oldSpec.getPaths() == null || newSpec.getPaths() == null) {
            return changes;
        }

        Set<String> oldPaths = oldSpec.getPaths().keySet();
        Set<String> newPaths = newSpec.getPaths().keySet();

        for (String path : oldPaths) {
            if (!newPaths.contains(path)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.ENDPOINT_REMOVED)
                        .severity(ChangeSeverity.CRITICAL)
                        .path(path)
                        .description("Endpoint wurde entfernt")
                        .oldValue(path)
                        .newValue(null)
                        .isBreakingChange(true)
                        .build());
            }
        }

        return changes;
    }

    @Override
    public String getRuleName() {
        return "Endpoint Removed Rule";
    }
}

