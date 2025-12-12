package com.oas.diff.web.dto;

import com.oas.diff.model.ComparisonResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO für Vergleichsergebnisse in REST-Responses.
 */
public class ComparisonResultDto {

    private String oldVersion;
    private String newVersion;
    private List<ApiChangeDto> changes;
    private List<ApiChangeDto> breakingChanges;
    private boolean hasBreakingChanges;
    private int totalChanges;
    private int breakingChangesCount;
    private long timestamp;

    public ComparisonResultDto() {
    }

    public static ComparisonResultDto fromModel(ComparisonResult result) {
        ComparisonResultDto dto = new ComparisonResultDto();
        dto.setOldVersion(result.getOldVersion());
        dto.setNewVersion(result.getNewVersion());
        dto.setChanges(result.getChanges().stream()
                .map(ApiChangeDto::fromModel)
                .collect(Collectors.toList()));
        dto.setBreakingChanges(result.getBreakingChanges().stream()
                .map(ApiChangeDto::fromModel)
                .collect(Collectors.toList()));
        dto.setHasBreakingChanges(result.hasBreakingChanges());
        dto.setTotalChanges(result.getTotalChangesCount());
        dto.setBreakingChangesCount(result.getBreakingChangesCount());
        dto.setTimestamp(result.getTimestamp());
        return dto;
    }

    // Getters and Setters
    public String getOldVersion() {
        return oldVersion;
    }

    public void setOldVersion(String oldVersion) {
        this.oldVersion = oldVersion;
    }

    public String getNewVersion() {
        return newVersion;
    }

    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }

    public List<ApiChangeDto> getChanges() {
        return changes;
    }

    public void setChanges(List<ApiChangeDto> changes) {
        this.changes = changes;
    }

    public List<ApiChangeDto> getBreakingChanges() {
        return breakingChanges;
    }

    public void setBreakingChanges(List<ApiChangeDto> breakingChanges) {
        this.breakingChanges = breakingChanges;
    }

    public boolean isHasBreakingChanges() {
        return hasBreakingChanges;
    }

    public void setHasBreakingChanges(boolean hasBreakingChanges) {
        this.hasBreakingChanges = hasBreakingChanges;
    }

    public int getTotalChanges() {
        return totalChanges;
    }

    public void setTotalChanges(int totalChanges) {
        this.totalChanges = totalChanges;
    }

    public int getBreakingChangesCount() {
        return breakingChangesCount;
    }

    public void setBreakingChangesCount(int breakingChangesCount) {
        this.breakingChangesCount = breakingChangesCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

