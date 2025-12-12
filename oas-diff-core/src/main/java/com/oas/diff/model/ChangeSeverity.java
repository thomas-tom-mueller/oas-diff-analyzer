package com.oas.diff.model;

/**
 * Repräsentiert die Schwere einer Änderung zwischen OAS-Versionen.
 */
public enum ChangeSeverity {
    /**
     * Kritische Änderung - Clients werden definitiv brechen
     */
    CRITICAL("Kritisch"),

    /**
     * Wichtige Änderung - Breaking Change, der die meisten Clients betrifft
     */
    MAJOR("Wichtig"),

    /**
     * Kleinere Änderung - Potentiell breaking für manche Clients
     */
    MINOR("Klein"),

    /**
     * Informativ - Keine Breaking Changes
     */
    INFO("Information");

    private final String displayName;

    ChangeSeverity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

