package com.oas.diff.rules;

import com.oas.diff.model.ApiChange;
import com.oas.diff.model.ChangeSeverity;
import com.oas.diff.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Regel: Erkennt Änderungen am Base-Path/Server-URL (Breaking Change).
 */
@Component
public class BasePathChangedRule implements BreakingChangeRule {

    @Override
    public List<ApiChange> evaluate(OpenAPI oldSpec, OpenAPI newSpec) {
        List<ApiChange> changes = new ArrayList<>();

        List<Server> oldServers = oldSpec.getServers();
        List<Server> newServers = newSpec.getServers();

        if (oldServers == null || oldServers.isEmpty() || newServers == null || newServers.isEmpty()) {
            return changes;
        }

        // Vergleiche den ersten/primären Server
        Server oldServer = oldServers.getFirst();
        Server newServer = newServers.getFirst();

        String oldUrl = oldServer.getUrl();
        String newUrl = newServer.getUrl();

        if (oldUrl != null && newUrl != null && !oldUrl.equals(newUrl)) {
            changes.add(ApiChange.builder()
                    .type(ChangeType.BASE_PATH_CHANGED)
                    .severity(ChangeSeverity.CRITICAL)
                    .path("Server")
                    .description("Base-Path/Server-URL geändert")
                    .oldValue(oldUrl)
                    .newValue(newUrl)
                    .isBreakingChange(true)
                    .build());
        }

        return changes;
    }

    @Override
    public String getRuleName() {
        return "Base Path Changed Rule";
    }
}

