package com.oas.diff.model;

/**
 * Repräsentiert eine einzelne Änderung zwischen zwei OAS-Versionen.
 */
public class ApiChange {

    private final ChangeType type;
    private final ChangeSeverity severity;
    private final String path;
    private final String description;
    private final String oldValue;
    private final String newValue;
    private final boolean isBreakingChange;

    private ApiChange(Builder builder) {
        this.type = builder.type;
        this.severity = builder.severity;
        this.path = builder.path;
        this.description = builder.description;
        this.oldValue = builder.oldValue;
        this.newValue = builder.newValue;
        this.isBreakingChange = builder.isBreakingChange;
    }

    public ChangeType getType() {
        return type;
    }

    public ChangeSeverity getSeverity() {
        return severity;
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public boolean isBreakingChange() {
        return isBreakingChange;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ChangeType type;
        private ChangeSeverity severity;
        private String path;
        private String description;
        private String oldValue;
        private String newValue;
        private boolean isBreakingChange;

        public Builder type(ChangeType type) {
            this.type = type;
            return this;
        }

        public Builder severity(ChangeSeverity severity) {
            this.severity = severity;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder oldValue(String oldValue) {
            this.oldValue = oldValue;
            return this;
        }

        public Builder newValue(String newValue) {
            this.newValue = newValue;
            return this;
        }

        public Builder isBreakingChange(boolean isBreakingChange) {
            this.isBreakingChange = isBreakingChange;
            return this;
        }

        public ApiChange build() {
            if (type == null || severity == null || path == null) {
                throw new IllegalStateException("Type, Severity und Path sind Pflichtfelder");
            }
            return new ApiChange(this);
        }
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s: %s (Breaking: %s)",
                severity.getDisplayName(),
                path,
                type.getDescription(),
                description,
                isBreakingChange);
    }
}

