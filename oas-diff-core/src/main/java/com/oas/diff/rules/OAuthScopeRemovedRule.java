package com.oas.diff.rules;

import com.oas.diff.model.ApiChange;
import com.oas.diff.model.ChangeSeverity;
import com.oas.diff.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regel: Erkennt entfernte OAuth2 Scopes (Breaking Change).
 * Wenn ein Scope entfernt wird, können Clients mit diesem Scope nicht mehr auf die API zugreifen.
 */
@Component
public class OAuthScopeRemovedRule implements BreakingChangeRule {

    @Override
    public List<ApiChange> evaluate(OpenAPI oldSpec, OpenAPI newSpec) {
        List<ApiChange> changes = new ArrayList<>();

        if (oldSpec.getComponents() == null || oldSpec.getComponents().getSecuritySchemes() == null ||
            newSpec.getComponents() == null || newSpec.getComponents().getSecuritySchemes() == null) {
            return changes;
        }

        Map<String, SecurityScheme> oldSchemes = oldSpec.getComponents().getSecuritySchemes();
        Map<String, SecurityScheme> newSchemes = newSpec.getComponents().getSecuritySchemes();

        for (Map.Entry<String, SecurityScheme> entry : oldSchemes.entrySet()) {
            String schemeName = entry.getKey();
            SecurityScheme oldScheme = entry.getValue();
            SecurityScheme newScheme = newSchemes.get(schemeName);

            if (newScheme == null || !SecurityScheme.Type.OAUTH2.equals(oldScheme.getType()) ||
                !SecurityScheme.Type.OAUTH2.equals(newScheme.getType())) {
                continue;
            }

            checkScopesInFlows(schemeName, oldScheme.getFlows(), newScheme.getFlows(), changes);
        }

        return changes;
    }

    private void checkScopesInFlows(String schemeName, OAuthFlows oldFlows, OAuthFlows newFlows,
                                   List<ApiChange> changes) {
        if (oldFlows == null || newFlows == null) {
            return;
        }

        // Prüfe Scopes in Authorization Code Flow
        if (oldFlows.getAuthorizationCode() != null && newFlows.getAuthorizationCode() != null) {
            checkFlowScopes(schemeName, "authorizationCode",
                          oldFlows.getAuthorizationCode(),
                          newFlows.getAuthorizationCode(),
                          changes);
        }

        // Prüfe Scopes in Implicit Flow
        if (oldFlows.getImplicit() != null && newFlows.getImplicit() != null) {
            checkFlowScopes(schemeName, "implicit",
                          oldFlows.getImplicit(),
                          newFlows.getImplicit(),
                          changes);
        }

        // Prüfe Scopes in Password Flow
        if (oldFlows.getPassword() != null && newFlows.getPassword() != null) {
            checkFlowScopes(schemeName, "password",
                          oldFlows.getPassword(),
                          newFlows.getPassword(),
                          changes);
        }

        // Prüfe Scopes in Client Credentials Flow
        if (oldFlows.getClientCredentials() != null && newFlows.getClientCredentials() != null) {
            checkFlowScopes(schemeName, "clientCredentials",
                          oldFlows.getClientCredentials(),
                          newFlows.getClientCredentials(),
                          changes);
        }
    }

    private void checkFlowScopes(String schemeName, String flowType, OAuthFlow oldFlow, OAuthFlow newFlow,
                                List<ApiChange> changes) {
        Map<String, String> oldScopes = oldFlow.getScopes();
        Map<String, String> newScopes = newFlow.getScopes();

        if (oldScopes == null || newScopes == null) {
            return;
        }

        Set<String> oldScopeNames = oldScopes.keySet();
        Set<String> newScopeNames = newScopes.keySet();

        // Prüfe auf entfernte Scopes
        for (String scopeName : oldScopeNames) {
            if (!newScopeNames.contains(scopeName)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.OAUTH_SCOPE_REMOVED)
                        .severity(ChangeSeverity.CRITICAL)
                        .path("Security Scheme: " + schemeName + " (" + flowType + ")")
                        .description("OAuth2 Scope entfernt: " + scopeName)
                        .oldValue(scopeName + ": " + oldScopes.get(scopeName))
                        .newValue(null)
                        .isBreakingChange(true)
                        .build());
            }
        }

        // Prüfe auf geänderte Scope-Beschreibungen (informativ, nicht breaking)
        for (String scopeName : oldScopeNames) {
            if (newScopeNames.contains(scopeName)) {
                String oldDescription = oldScopes.get(scopeName);
                String newDescription = newScopes.get(scopeName);

                if (oldDescription != null && newDescription != null &&
                    !oldDescription.equals(newDescription)) {
                    // Beschreibungsänderung ist nicht breaking, aber könnte interessant sein
                    // Wird hier nicht gemeldet, könnte aber in einer separaten Rule erfasst werden
                }
            }
        }
    }

    @Override
    public String getRuleName() {
        return "OAuth Scope Removed Rule";
    }
}

