package com.majtom.oas.rules.minor;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import com.majtom.oas.rules.BreakingChangeRule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt entfernte HATEOAS Links (Breaking Change).
 * Wenn Links entfernt werden, können Clients die darauf angewiesen sind, brechen.
 */
@Component
public class LinkRemovedRule implements BreakingChangeRule {

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

            checkOperationLinks(path, "GET", oldPathItem.getGet(), newPathItem.getGet(), changes);
            checkOperationLinks(path, "POST", oldPathItem.getPost(), newPathItem.getPost(), changes);
            checkOperationLinks(path, "PUT", oldPathItem.getPut(), newPathItem.getPut(), changes);
            checkOperationLinks(path, "DELETE", oldPathItem.getDelete(), newPathItem.getDelete(), changes);
        }

        return changes;
    }

    private void checkOperationLinks(String path, String method, Operation oldOp, Operation newOp,
                                     List<ApiChange> changes) {
        if (oldOp == null || newOp == null) {
            return;
        }

        if (oldOp.getResponses() == null || newOp.getResponses() == null) {
            return;
        }

        for (Map.Entry<String, ApiResponse> entry : oldOp.getResponses().entrySet()) {
            String statusCode = entry.getKey();
            ApiResponse oldResponse = entry.getValue();
            ApiResponse newResponse = newOp.getResponses().get(statusCode);

            if (newResponse == null) {
                continue;
            }

            checkResponseLinks(path, method, statusCode, oldResponse, newResponse, changes);
        }
    }

    private void checkResponseLinks(String path, String method, String statusCode,
                                    ApiResponse oldResponse, ApiResponse newResponse,
                                    List<ApiChange> changes) {
        Map<String, Link> oldLinks = oldResponse.getLinks();
        Map<String, Link> newLinks = newResponse.getLinks();

        if (oldLinks == null || oldLinks.isEmpty()) {
            return;
        }

        if (newLinks == null || newLinks.isEmpty()) {
            // Alle Links entfernt
            for (String linkName : oldLinks.keySet()) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.LINK_REMOVED)
                        .severity(ChangeSeverity.MINOR)
                        .path(path + " [" + method + "] Response: " + statusCode)
                        .description("HATEOAS Link entfernt: " + linkName)
                        .oldValue(linkName)
                        .newValue(null)
                        .isBreakingChange(true)
                        .build());
            }
            return;
        }

        // Prüfe auf entfernte Links
        for (String linkName : oldLinks.keySet()) {
            if (!newLinks.containsKey(linkName)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.LINK_REMOVED)
                        .severity(ChangeSeverity.MINOR)
                        .path(path + " [" + method + "] Response: " + statusCode)
                        .description("HATEOAS Link entfernt: " + linkName)
                        .oldValue(linkName)
                        .newValue(null)
                        .isBreakingChange(true)
                        .build());
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Link Removed Rule";
    }
}

