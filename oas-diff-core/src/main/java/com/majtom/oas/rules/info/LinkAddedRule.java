package com.majtom.oas.rules.info;

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
 * Regel: Erkennt hinzugefügte HATEOAS Links (Non-Breaking).
 * Neue Links erweitern die Hypermedia-Navigation ohne Breaking Changes.
 */
@Component
public class LinkAddedRule implements BreakingChangeRule {

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

        for (Map.Entry<String, ApiResponse> entry : newOp.getResponses().entrySet()) {
            String statusCode = entry.getKey();
            ApiResponse newResponse = entry.getValue();
            ApiResponse oldResponse = oldOp.getResponses().get(statusCode);

            if (oldResponse == null) {
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

        if (newLinks == null || newLinks.isEmpty()) {
            return;
        }

        if (oldLinks == null || oldLinks.isEmpty()) {
            // Alle Links neu hinzugefügt
            for (String linkName : newLinks.keySet()) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.LINK_ADDED)
                        .severity(ChangeSeverity.INFO)
                        .path(path + " [" + method + "] Response: " + statusCode)
                        .description("HATEOAS Link hinzugefügt: " + linkName)
                        .oldValue(null)
                        .newValue(linkName)
                        .isBreakingChange(false)
                        .build());
            }
            return;
        }

        // Prüfe auf neue Links
        for (String linkName : newLinks.keySet()) {
            if (!oldLinks.containsKey(linkName)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.LINK_ADDED)
                        .severity(ChangeSeverity.INFO)
                        .path(path + " [" + method + "] Response: " + statusCode)
                        .description("HATEOAS Link hinzugefügt: " + linkName)
                        .oldValue(null)
                        .newValue(linkName)
                        .isBreakingChange(false)
                        .build());
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Link Added Rule";
    }
}

