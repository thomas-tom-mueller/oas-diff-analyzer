package com.majtom.oas.rules.critical;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import com.majtom.oas.rules.BreakingChangeRule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt hinzugefügte Security-Requirements (Breaking Change).
 * Wenn eine Operation plötzlich Authentication benötigt, ist das ein kritischer Breaking Change.
 */
@Component
public class SecurityRequirementAddedRule implements BreakingChangeRule {

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
                continue; // Neuer Endpoint, wird von EndpointAddedRule behandelt
            }

            checkOperationSecurity(path, "GET", oldPathItem.getGet(), newPathItem.getGet(), changes);
            checkOperationSecurity(path, "POST", oldPathItem.getPost(), newPathItem.getPost(), changes);
            checkOperationSecurity(path, "PUT", oldPathItem.getPut(), newPathItem.getPut(), changes);
            checkOperationSecurity(path, "DELETE", oldPathItem.getDelete(), newPathItem.getDelete(), changes);
            checkOperationSecurity(path, "PATCH", oldPathItem.getPatch(), newPathItem.getPatch(), changes);
            checkOperationSecurity(path, "HEAD", oldPathItem.getHead(), newPathItem.getHead(), changes);
            checkOperationSecurity(path, "OPTIONS", oldPathItem.getOptions(), newPathItem.getOptions(), changes);
        }

        return changes;
    }

    private void checkOperationSecurity(String path, String method, Operation oldOp, Operation newOp,
                                       List<ApiChange> changes) {
        if (oldOp == null || newOp == null) {
            return;
        }

        List<SecurityRequirement> oldSecurity = oldOp.getSecurity() != null ? oldOp.getSecurity() : new ArrayList<>();
        List<SecurityRequirement> newSecurity = newOp.getSecurity() != null ? newOp.getSecurity() : new ArrayList<>();

        // Prüfe ob neue Security Requirements hinzugefügt wurden
        if (oldSecurity.isEmpty() && !newSecurity.isEmpty()) {
            // Operation hatte keine Security, hat aber jetzt welche
            String securitySchemes = getSecuritySchemeNames(newSecurity);
            changes.add(ApiChange.builder()
                    .type(ChangeType.SECURITY_REQUIREMENT_ADDED)
                    .severity(ChangeSeverity.CRITICAL)
                    .path(path + " [" + method + "]")
                    .description("Security-Anforderung hinzugefügt: " + securitySchemes)
                    .oldValue("keine Authentication")
                    .newValue(securitySchemes)
                    .isBreakingChange(true)
                    .build());
        } else if (!oldSecurity.isEmpty() && !newSecurity.isEmpty()) {
            // Prüfe ob zusätzliche Security Schemes hinzugefügt wurden
            for (SecurityRequirement newReq : newSecurity) {
                if (!containsSecurityRequirement(oldSecurity, newReq)) {
                    String schemeName = getSecuritySchemeNames(List.of(newReq));
                    changes.add(ApiChange.builder()
                            .type(ChangeType.SECURITY_REQUIREMENT_ADDED)
                            .severity(ChangeSeverity.MAJOR)
                            .path(path + " [" + method + "]")
                            .description("Zusätzliche Security-Anforderung hinzugefügt: " + schemeName)
                            .oldValue(getSecuritySchemeNames(oldSecurity))
                            .newValue(getSecuritySchemeNames(newSecurity))
                            .isBreakingChange(true)
                            .build());
                }
            }
        }
    }

    private boolean containsSecurityRequirement(List<SecurityRequirement> list, SecurityRequirement requirement) {
        for (SecurityRequirement req : list) {
            if (req.keySet().equals(requirement.keySet())) {
                return true;
            }
        }
        return false;
    }

    private String getSecuritySchemeNames(List<SecurityRequirement> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            return "none";
        }

        List<String> schemes = new ArrayList<>();
        for (SecurityRequirement req : requirements) {
            schemes.addAll(req.keySet());
        }
        return String.join(", ", schemes);
    }

    @Override
    public String getRuleName() {
        return "Security Requirement Added Rule";
    }
}

