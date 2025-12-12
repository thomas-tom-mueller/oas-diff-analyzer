package com.oas.diff.model;

/**
 * Repräsentiert den Typ einer Änderung in der API-Spezifikation.
 */
public enum ChangeType {
    ENDPOINT_REMOVED("Endpoint entfernt"),
    ENDPOINT_ADDED("Endpoint hinzugefügt"),
    METHOD_REMOVED("HTTP-Methode entfernt"),
    METHOD_ADDED("HTTP-Methode hinzugefügt"),
    PARAMETER_REMOVED("Parameter entfernt"),
    PARAMETER_ADDED("Parameter hinzugefügt"),
    PARAMETER_REQUIRED_ADDED("Parameter wurde required"),
    PARAMETER_TYPE_CHANGED("Parameter-Typ geändert"),
    RESPONSE_CODE_REMOVED("Response-Code entfernt"),
    RESPONSE_CODE_CHANGED("Response-Code geändert"),
    RESPONSE_SCHEMA_CHANGED("Response-Schema geändert"),
    REQUEST_SCHEMA_CHANGED("Request-Schema geändert"),
    PROPERTY_REMOVED("Property entfernt"),
    PROPERTY_ADDED("Property hinzugefügt"),
    PROPERTY_REQUIRED_ADDED("Property wurde required"),
    PROPERTY_TYPE_CHANGED("Property-Typ geändert"),
    ENUM_VALUE_REMOVED("Enum-Wert entfernt"),
    ENUM_VALUE_ADDED("Enum-Wert hinzugefügt"),
    API_VERSION_CHANGED("API-Version geändert"),
    BASE_PATH_CHANGED("Base-Path geändert");

    private final String description;

    ChangeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

