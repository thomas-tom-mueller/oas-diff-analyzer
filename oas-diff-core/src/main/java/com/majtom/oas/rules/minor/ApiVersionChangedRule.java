package com.majtom.oas.rules.minor;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;
import com.majtom.oas.rules.BreakingChangeRule;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Regel: Erkennt Änderungen der API-Version (Breaking Change).
 */
@Component
public class ApiVersionChangedRule implements BreakingChangeRule {

    @Override
    public List<ApiChange> evaluate(OpenAPI oldSpec, OpenAPI newSpec) {
        List<ApiChange> changes = new ArrayList<>();

        if (oldSpec.getInfo() == null || newSpec.getInfo() == null) {
            return changes;
        }

        String oldVersion = oldSpec.getInfo().getVersion();
        String newVersion = newSpec.getInfo().getVersion();

        if (oldVersion != null && newVersion != null && !oldVersion.equals(newVersion)) {
            changes.add(ApiChange.builder()
                    .type(ChangeType.API_VERSION_CHANGED)
                    .severity(determineVersionChangeSeverity(oldVersion, newVersion))
                    .path("Info")
                    .description("API-Version geändert")
                    .oldValue(oldVersion)
                    .newValue(newVersion)
                    .isBreakingChange(isMajorVersionChange(oldVersion, newVersion))
                    .build());
        }

        return changes;
    }

    private ChangeSeverity determineVersionChangeSeverity(String oldVersion, String newVersion) {
        if (isMajorVersionChange(oldVersion, newVersion)) {
            return ChangeSeverity.CRITICAL;
        } else if (isMinorVersionChange(oldVersion, newVersion)) {
            return ChangeSeverity.MINOR;
        } else {
            return ChangeSeverity.INFO;
        }
    }

    private boolean isMajorVersionChange(String oldVersion, String newVersion) {
        try {
            String oldMajor = extractMajorVersion(oldVersion);
            String newMajor = extractMajorVersion(newVersion);
            return !oldMajor.equals(newMajor);
        } catch (Exception e) {
            return true; // Bei Unsicherheit als Major-Change behandeln
        }
    }

    private boolean isMinorVersionChange(String oldVersion, String newVersion) {
        try {
            String[] oldParts = oldVersion.split("\\.");
            String[] newParts = newVersion.split("\\.");

            if (oldParts.length >= 2 && newParts.length >= 2) {
                return oldParts[0].equals(newParts[0]) && !oldParts[1].equals(newParts[1]);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private String extractMajorVersion(String version) {
        if (version == null || version.isEmpty()) {
            return "0";
        }
        String[] parts = version.split("\\.");
        return parts.length > 0 ? parts[0] : "0";
    }

    @Override
    public String getRuleName() {
        return "API Version Changed Rule";
    }
}

