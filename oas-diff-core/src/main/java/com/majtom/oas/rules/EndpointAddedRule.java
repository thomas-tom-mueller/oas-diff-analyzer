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
 * Regel: Erkennt hinzugefügte Endpoints (Non-Breaking Change).
 */
@Component
public class EndpointAddedRule implements BreakingChangeRule {

    @Override
    public List<ApiChange> evaluate(OpenAPI oldSpec, OpenAPI newSpec) {
        List<ApiChange> changes = new ArrayList<>();

        if (oldSpec.getPaths() == null || newSpec.getPaths() == null) {
            return changes;
        }

        Set<String> oldPaths = oldSpec.getPaths().keySet();
        Set<String> newPaths = newSpec.getPaths().keySet();

        for (String path : newPaths) {
            if (!oldPaths.contains(path)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.ENDPOINT_ADDED)
                        .severity(ChangeSeverity.INFO)
                        .path(path)
                        .description("Neuer Endpoint hinzugefügt")
                        .oldValue(null)
                        .newValue(path)
                        .isBreakingChange(false)
                        .build());
            }
        }

        return changes;
    }

    @Override
    public String getRuleName() {
        return "Endpoint Added Rule";
    }
}

