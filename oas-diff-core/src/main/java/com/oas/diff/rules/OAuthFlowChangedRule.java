package com.oas.diff.rules;

import com.oas.diff.model.ApiChange;
import com.oas.diff.model.ChangeSeverity;
import com.oas.diff.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt Änderungen am OAuth2 Flow (Breaking Change).
 * Z.B. Wechsel von Authorization Code zu Client Credentials Flow.
 */
@Component
public class OAuthFlowChangedRule implements BreakingChangeRule {

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

            if (newScheme == null || !SecurityScheme.Type.OAUTH2.equals(oldScheme.getType())) {
                continue;
            }

            if (SecurityScheme.Type.OAUTH2.equals(newScheme.getType())) {
                checkOAuthFlows(schemeName, oldScheme, newScheme, changes);
            }
        }

        return changes;
    }

    private void checkOAuthFlows(String schemeName, SecurityScheme oldScheme, SecurityScheme newScheme,
                                 List<ApiChange> changes) {
        OAuthFlows oldFlows = oldScheme.getFlows();
        OAuthFlows newFlows = newScheme.getFlows();

        if (oldFlows == null || newFlows == null) {
            return;
        }

        // Prüfe Authorization Code Flow
        if (oldFlows.getAuthorizationCode() != null && newFlows.getAuthorizationCode() == null) {
            changes.add(ApiChange.builder()
                    .type(ChangeType.OAUTH_FLOW_CHANGED)
                    .severity(ChangeSeverity.CRITICAL)
                    .path("Security Scheme: " + schemeName)
                    .description("OAuth2 Authorization Code Flow entfernt")
                    .oldValue("authorizationCode")
                    .newValue("entfernt")
                    .isBreakingChange(true)
                    .build());
        }

        // Prüfe Implicit Flow
        if (oldFlows.getImplicit() != null && newFlows.getImplicit() == null) {
            changes.add(ApiChange.builder()
                    .type(ChangeType.OAUTH_FLOW_CHANGED)
                    .severity(ChangeSeverity.CRITICAL)
                    .path("Security Scheme: " + schemeName)
                    .description("OAuth2 Implicit Flow entfernt")
                    .oldValue("implicit")
                    .newValue("entfernt")
                    .isBreakingChange(true)
                    .build());
        }

        // Prüfe Password Flow
        if (oldFlows.getPassword() != null && newFlows.getPassword() == null) {
            changes.add(ApiChange.builder()
                    .type(ChangeType.OAUTH_FLOW_CHANGED)
                    .severity(ChangeSeverity.CRITICAL)
                    .path("Security Scheme: " + schemeName)
                    .description("OAuth2 Password Flow entfernt")
                    .oldValue("password")
                    .newValue("entfernt")
                    .isBreakingChange(true)
                    .build());
        }

        // Prüfe Client Credentials Flow
        if (oldFlows.getClientCredentials() != null && newFlows.getClientCredentials() == null) {
            changes.add(ApiChange.builder()
                    .type(ChangeType.OAUTH_FLOW_CHANGED)
                    .severity(ChangeSeverity.CRITICAL)
                    .path("Security Scheme: " + schemeName)
                    .description("OAuth2 Client Credentials Flow entfernt")
                    .oldValue("clientCredentials")
                    .newValue("entfernt")
                    .isBreakingChange(true)
                    .build());
        }

        // Prüfe ob alle Flows geändert wurden
        String oldFlowTypes = getAvailableFlows(oldFlows);
        String newFlowTypes = getAvailableFlows(newFlows);

        if (!oldFlowTypes.equals(newFlowTypes) && !changes.isEmpty()) {
            // Flows haben sich geändert - wurde bereits oben erfasst
            return;
        }

        // Prüfe URLs bei vorhandenen Flows
        checkFlowUrls(schemeName, oldFlows, newFlows, changes);
    }

    private void checkFlowUrls(String schemeName, OAuthFlows oldFlows, OAuthFlows newFlows,
                              List<ApiChange> changes) {
        // Authorization URL geändert
        if (oldFlows.getAuthorizationCode() != null && newFlows.getAuthorizationCode() != null) {
            String oldUrl = oldFlows.getAuthorizationCode().getAuthorizationUrl();
            String newUrl = newFlows.getAuthorizationCode().getAuthorizationUrl();

            if (oldUrl != null && newUrl != null && !oldUrl.equals(newUrl)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.OAUTH_FLOW_CHANGED)
                        .severity(ChangeSeverity.CRITICAL)
                        .path("Security Scheme: " + schemeName)
                        .description("OAuth2 Authorization URL geändert")
                        .oldValue(oldUrl)
                        .newValue(newUrl)
                        .isBreakingChange(true)
                        .build());
            }

            // Token URL geändert
            String oldTokenUrl = oldFlows.getAuthorizationCode().getTokenUrl();
            String newTokenUrl = newFlows.getAuthorizationCode().getTokenUrl();

            if (oldTokenUrl != null && newTokenUrl != null && !oldTokenUrl.equals(newTokenUrl)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.OAUTH_FLOW_CHANGED)
                        .severity(ChangeSeverity.CRITICAL)
                        .path("Security Scheme: " + schemeName)
                        .description("OAuth2 Token URL geändert")
                        .oldValue(oldTokenUrl)
                        .newValue(newTokenUrl)
                        .isBreakingChange(true)
                        .build());
            }
        }
    }

    private String getAvailableFlows(OAuthFlows flows) {
        List<String> availableFlows = new ArrayList<>();

        if (flows.getAuthorizationCode() != null) {
            availableFlows.add("authorizationCode");
        }
        if (flows.getImplicit() != null) {
            availableFlows.add("implicit");
        }
        if (flows.getPassword() != null) {
            availableFlows.add("password");
        }
        if (flows.getClientCredentials() != null) {
            availableFlows.add("clientCredentials");
        }

        return String.join(", ", availableFlows);
    }

    @Override
    public String getRuleName() {
        return "OAuth Flow Changed Rule";
    }
}

