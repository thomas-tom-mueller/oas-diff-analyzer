package com.majtom.oas.web.dto;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ChangeSeverity;
import com.majtom.oas.model.ChangeType;

/**
 * DTO für API-Änderungen in REST-Responses.
 */
public class ApiChangeDto {

    private ChangeType type;
    private ChangeSeverity severity;
    private String path;
    private String description;
    private String oldValue;
    private String newValue;
    private boolean breakingChange;

    public ApiChangeDto() {
    }

    public static ApiChangeDto fromModel(ApiChange change) {
        ApiChangeDto dto = new ApiChangeDto();
        dto.setType(change.getType());
        dto.setSeverity(change.getSeverity());
        dto.setPath(change.getPath());
        dto.setDescription(change.getDescription());
        dto.setOldValue(change.getOldValue());
        dto.setNewValue(change.getNewValue());
        dto.setBreakingChange(change.isBreakingChange());
        return dto;
    }

    // Getters and Setters
    public ChangeType getType() {
        return type;
    }

    public void setType(ChangeType type) {
        this.type = type;
    }

    public ChangeSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ChangeSeverity severity) {
        this.severity = severity;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public boolean isBreakingChange() {
        return breakingChange;
    }

    public void setBreakingChange(boolean breakingChange) {
        this.breakingChange = breakingChange;
    }
}

