package com.oas.diff.rules;

import com.oas.diff.model.ApiChange;
import com.oas.diff.model.ChangeSeverity;
import com.oas.diff.model.ChangeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regel: Erkennt entfernte Security-Requirements (Non-Breaking).
 * Wenn Authentication-Anforderungen entfernt werden, wird die API weniger restriktiv (kein Breaking Change).
 */
@Component
public class SecurityRequirementRemovedRule implements BreakingChangeRule {

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

            checkOperationSecurity(path, "GET", oldPathItem.getGet(), newPathItem.getGet(), changes);
            checkOperationSecurity(path, "POST", oldPathItem.getPost(), newPathItem.getPost(), changes);
            checkOperationSecurity(path, "PUT", oldPathItem.getPut(), newPathItem.getPut(), changes);
            checkOperationSecurity(path, "DELETE", oldPathItem.getDelete(), newPathItem.getDelete(), changes);
            checkOperationSecurity(path, "PATCH", oldPathItem.getPatch(), newPathItem.getPatch(), changes);
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

        // Security wurde komplett entfernt
        if (!oldSecurity.isEmpty() && newSecurity.isEmpty()) {
            String securitySchemes = getSecuritySchemeNames(oldSecurity);
            changes.add(ApiChange.builder()
                    .type(ChangeType.SECURITY_REQUIREMENT_REMOVED)
                    .severity(ChangeSeverity.INFO)
                    .path(path + " [" + method + "]")
                    .description("Security-Anforderung entfernt (weniger restriktiv): " + securitySchemes)
                    .oldValue(securitySchemes)
                    .newValue("keine Authentication")
                    .isBreakingChange(false)
                    .build());
        }
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
        return "Security Requirement Removed Rule";
    }
}

