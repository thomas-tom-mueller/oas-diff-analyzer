package com.majtom.oas.rules;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt hinzugefügte OAuth2 Scopes zu Operationen (Breaking Change).
 * Wenn ein zusätzlicher Scope erforderlich wird, können Clients ohne diesen Scope nicht mehr zugreifen.
 */
@Component
public class OAuthScopeAddedRule implements BreakingChangeRule {

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

            checkOperationScopes(path, "GET", oldPathItem.getGet(), newPathItem.getGet(), changes);
            checkOperationScopes(path, "POST", oldPathItem.getPost(), newPathItem.getPost(), changes);
            checkOperationScopes(path, "PUT", oldPathItem.getPut(), newPathItem.getPut(), changes);
            checkOperationScopes(path, "DELETE", oldPathItem.getDelete(), newPathItem.getDelete(), changes);
            checkOperationScopes(path, "PATCH", oldPathItem.getPatch(), newPathItem.getPatch(), changes);
        }

        return changes;
    }

    private void checkOperationScopes(String path, String method, Operation oldOp, Operation newOp,
                                     List<ApiChange> changes) {
        if (oldOp == null || newOp == null) {
            return;
        }

        List<SecurityRequirement> oldSecurity = oldOp.getSecurity() != null ? oldOp.getSecurity() : new ArrayList<>();
        List<SecurityRequirement> newSecurity = newOp.getSecurity() != null ? newOp.getSecurity() : new ArrayList<>();

        // Prüfe ob neue Scopes zu bestehenden Security-Anforderungen hinzugefügt wurden
        for (SecurityRequirement newReq : newSecurity) {
            for (Map.Entry<String, List<String>> entry : newReq.entrySet()) {
                String schemeName = entry.getKey();
                List<String> newScopes = entry.getValue();

                // Finde entsprechende alte Anforderung
                List<String> oldScopes = findScopesForScheme(oldSecurity, schemeName);

                if (oldScopes != null) {
                    // Prüfe auf neue Scopes
                    for (String scope : newScopes) {
                        if (!oldScopes.contains(scope)) {
                            changes.add(ApiChange.builder()
                                    .type(ChangeType.OAUTH_SCOPE_ADDED)
                                    .severity(ChangeSeverity.MAJOR)
                                    .path(path + " [" + method + "]")
                                    .description("OAuth2 Scope hinzugefügt: " + scope + " (für " + schemeName + ")")
                                    .oldValue(String.join(", ", oldScopes))
                                    .newValue(String.join(", ", newScopes))
                                    .isBreakingChange(true)
                                    .build());
                        }
                    }
                }
            }
        }
    }

    private List<String> findScopesForScheme(List<SecurityRequirement> securityList, String schemeName) {
        for (SecurityRequirement req : securityList) {
            if (req.containsKey(schemeName)) {
                return req.get(schemeName);
            }
        }
        return null;
    }

    @Override
    public String getRuleName() {
        return "OAuth Scope Added Rule";
    }
}

