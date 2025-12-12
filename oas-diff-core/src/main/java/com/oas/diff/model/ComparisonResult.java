package com.oas.diff.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fasst alle Änderungen zwischen zwei OAS-Versionen zusammen.
 */
public class ComparisonResult {

    private final String oldVersion;
    private final String newVersion;
    private final List<ApiChange> changes;
    private final long timestamp;

    public ComparisonResult(String oldVersion, String newVersion, List<ApiChange> changes) {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
        this.changes = new ArrayList<>(changes);
        this.timestamp = System.currentTimeMillis();
    }

    public String getOldVersion() {
        return oldVersion;
    }

    public String getNewVersion() {
        return newVersion;
    }

    public List<ApiChange> getChanges() {
        return Collections.unmodifiableList(changes);
    }

    public List<ApiChange> getBreakingChanges() {
        return changes.stream()
                .filter(ApiChange::isBreakingChange)
                .collect(Collectors.toList());
    }

    public List<ApiChange> getNonBreakingChanges() {
        return changes.stream()
                .filter(change -> !change.isBreakingChange())
                .collect(Collectors.toList());
    }

    public boolean hasBreakingChanges() {
        return changes.stream().anyMatch(ApiChange::isBreakingChange);
    }

    public int getTotalChangesCount() {
        return changes.size();
    }

    public int getBreakingChangesCount() {
        return (int) changes.stream().filter(ApiChange::isBreakingChange).count();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSummary() {
        return String.format(
                "Vergleich: %s → %s | Änderungen gesamt: %d | Breaking Changes: %d | Nicht-breaking: %d",
                oldVersion,
                newVersion,
                getTotalChangesCount(),
                getBreakingChangesCount(),
                getTotalChangesCount() - getBreakingChangesCount()
        );
    }
}

