package com.oas.diff.model;

/**
 * Repräsentiert die Schwere einer Änderung zwischen OAS-Versionen.
 *
 * Sortierung von kritisch zu informativ:
 * CRITICAL > MAJOR > MINOR > WARNING > INFO
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
     * Warnung - Zukünftige Breaking Changes (Deprecations)
     * Noch nicht breaking, aber Handlungsbedarf für die Zukunft
     */
    WARNING("Warnung"),

    /**
     * Informativ - Keine Breaking Changes, neue Features
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

