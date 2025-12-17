package com.majtom.oas.rules.critical;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import com.majtom.oas.rules.BreakingChangeRule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt Änderungen am Security-Schema-Typ (Breaking Change).
 * Z.B. Wechsel von OAuth2 zu API-Key Authentication.
 */
@Component
public class SecuritySchemeChangedRule implements BreakingChangeRule {

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

            if (newScheme == null) {
                continue; // Schema entfernt, könnte separate Rule sein
            }

            checkSchemeType(schemeName, oldScheme, newScheme, changes);
            checkSchemeDetails(schemeName, oldScheme, newScheme, changes);
        }

        return changes;
    }

    private void checkSchemeType(String schemeName, SecurityScheme oldScheme, SecurityScheme newScheme,
                                 List<ApiChange> changes) {
        SecurityScheme.Type oldType = oldScheme.getType();
        SecurityScheme.Type newType = newScheme.getType();

        if (oldType != null && newType != null && !oldType.equals(newType)) {
            changes.add(ApiChange.builder()
                    .type(ChangeType.SECURITY_SCHEME_CHANGED)
                    .severity(ChangeSeverity.CRITICAL)
                    .path("Security Scheme: " + schemeName)
                    .description("Security-Schema-Typ geändert")
                    .oldValue(oldType.toString())
                    .newValue(newType.toString())
                    .isBreakingChange(true)
                    .build());
        }
    }

    private void checkSchemeDetails(String schemeName, SecurityScheme oldScheme, SecurityScheme newScheme,
                                   List<ApiChange> changes) {
        // Prüfe In-Location bei API-Key
        if (SecurityScheme.Type.APIKEY.equals(oldScheme.getType()) &&
            SecurityScheme.Type.APIKEY.equals(newScheme.getType())) {

            SecurityScheme.In oldIn = oldScheme.getIn();
            SecurityScheme.In newIn = newScheme.getIn();

            if (oldIn != null && newIn != null && !oldIn.equals(newIn)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.SECURITY_SCHEME_CHANGED)
                        .severity(ChangeSeverity.CRITICAL)
                        .path("Security Scheme: " + schemeName)
                        .description("API-Key Location geändert")
                        .oldValue(oldIn.toString())
                        .newValue(newIn.toString())
                        .isBreakingChange(true)
                        .build());
            }

            // Prüfe Parameter Name
            String oldName = oldScheme.getName();
            String newName = newScheme.getName();

            if (oldName != null && newName != null && !oldName.equals(newName)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.SECURITY_SCHEME_CHANGED)
                        .severity(ChangeSeverity.CRITICAL)
                        .path("Security Scheme: " + schemeName)
                        .description("API-Key Parameter-Name geändert")
                        .oldValue(oldName)
                        .newValue(newName)
                        .isBreakingChange(true)
                        .build());
            }
        }

        // Prüfe Bearer Format
        if (SecurityScheme.Type.HTTP.equals(oldScheme.getType()) &&
            SecurityScheme.Type.HTTP.equals(newScheme.getType())) {

            String oldSchemeStr = oldScheme.getScheme();
            String newSchemeStr = newScheme.getScheme();

            if (oldSchemeStr != null && newSchemeStr != null && !oldSchemeStr.equals(newSchemeStr)) {
                changes.add(ApiChange.builder()
                        .type(ChangeType.SECURITY_SCHEME_CHANGED)
                        .severity(ChangeSeverity.CRITICAL)
                        .path("Security Scheme: " + schemeName)
                        .description("HTTP Authentication Scheme geändert")
                        .oldValue(oldSchemeStr)
                        .newValue(newSchemeStr)
                        .isBreakingChange(true)
                        .build());
            }
        }
    }

    @Override
    public String getRuleName() {
        return "Security Scheme Changed Rule";
    }
}

