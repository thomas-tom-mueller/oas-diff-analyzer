package com.oas.diff.model;

/**
 * Repräsentiert den Typ einer Änderung in der API-Spezifikation.
 *
 * Die Enum-Werte sind nach Priorität/Severity sortiert:
 * 1. CRITICAL Breaking Changes (Endpoints, Methods, Security)
 * 2. MAJOR Breaking Changes (Parameter, Properties, Constraints)
 * 3. MINOR Changes (Deprecations, Headers)
 * 4. INFO/Non-Breaking Changes (Additions)
 */
public enum ChangeType {

    // ========================================
    // CRITICAL BREAKING CHANGES
    // ========================================

    // Endpoints & Methods
    ENDPOINT_REMOVED("Endpoint entfernt"),
    METHOD_REMOVED("HTTP-Methode entfernt"),

    // Security & Authentication
    SECURITY_REQUIREMENT_ADDED("Security-Anforderung hinzugefügt"),
    SECURITY_SCHEME_CHANGED("Security-Schema geändert"),
    OAUTH_FLOW_CHANGED("OAuth-Flow geändert"),
    OAUTH_SCOPE_REMOVED("OAuth-Scope entfernt"),

    // Request/Response Body
    REQUEST_BODY_REMOVED("Request-Body entfernt"),
    REQUEST_BODY_REQUIRED_ADDED("Request-Body wurde required"),
    REQUEST_CONTENT_TYPE_REMOVED("Request Content-Type entfernt"),
    RESPONSE_CONTENT_TYPE_REMOVED("Response Content-Type entfernt"),

    // Response Codes
    RESPONSE_CODE_REMOVED("Response-Code entfernt"),
    RESPONSE_CODE_CHANGED("Response-Code geändert"),

    // API Metadata
    API_VERSION_CHANGED("API-Version geändert"),
    BASE_PATH_CHANGED("Base-Path geändert"),

    // ========================================
    // MAJOR BREAKING CHANGES
    // ========================================

    // Parameters
    PARAMETER_REMOVED("Parameter entfernt"),
    PARAMETER_REQUIRED_ADDED("Parameter wurde required"),
    PARAMETER_TYPE_CHANGED("Parameter-Typ geändert"),
    PARAMETER_LOCATION_CHANGED("Parameter-Location geändert"),
    PARAMETER_STYLE_CHANGED("Parameter-Style geändert"),
    PARAMETER_EXPLODE_CHANGED("Parameter-Explode geändert"),

    // Schemas
    REQUEST_SCHEMA_CHANGED("Request-Schema geändert"),
    RESPONSE_SCHEMA_CHANGED("Response-Schema geändert"),

    // Properties
    PROPERTY_REMOVED("Property entfernt"),
    PROPERTY_REQUIRED_ADDED("Property wurde required"),
    PROPERTY_TYPE_CHANGED("Property-Typ geändert"),
    PROPERTY_FORMAT_CHANGED("Property-Format geändert"),

    // String Constraints
    PROPERTY_PATTERN_ADDED("Property-Pattern hinzugefügt"),
    PROPERTY_PATTERN_CHANGED("Property-Pattern geändert"),
    PROPERTY_MIN_LENGTH_INCREASED("Property minLength erhöht"),
    PROPERTY_MAX_LENGTH_DECREASED("Property maxLength verringert"),

    // Number Constraints
    PROPERTY_MINIMUM_INCREASED("Property Minimum erhöht"),
    PROPERTY_MAXIMUM_DECREASED("Property Maximum verringert"),

    // Array Constraints
    ARRAY_MIN_ITEMS_INCREASED("Array minItems erhöht"),
    ARRAY_MAX_ITEMS_DECREASED("Array maxItems verringert"),
    ARRAY_UNIQUE_ITEMS_ADDED("Array uniqueItems hinzugefügt"),

    // Enum Values
    ENUM_VALUE_REMOVED("Enum-Wert entfernt"),

    // Schema Structure
    DISCRIMINATOR_CHANGED("Discriminator geändert"),
    ONE_OF_OPTION_REMOVED("OneOf-Option entfernt"),
    ADDITIONAL_PROPERTIES_FORBIDDEN("AdditionalProperties verboten"),
    ADDITIONAL_PROPERTIES_TYPE_CHANGED("AdditionalProperties-Typ geändert"),

    // Behavior Changes
    DEFAULT_VALUE_CHANGED("Default-Wert geändert"),

    // OAuth Details
    OAUTH_SCOPE_ADDED("OAuth-Scope hinzugefügt"),

    // Response Headers
    RESPONSE_HEADER_REMOVED("Response-Header entfernt"),

    // ========================================
    // MINOR CHANGES
    // ========================================

    // Property Flags
    PROPERTY_READ_ONLY_CHANGED("Property readOnly geändert"),
    PROPERTY_WRITE_ONLY_CHANGED("Property writeOnly geändert"),

    // Defaults
    DEFAULT_VALUE_REMOVED("Default-Wert entfernt"),

    // Callbacks & Links
    CALLBACK_REMOVED("Callback entfernt"),
    CALLBACK_URL_CHANGED("Callback-URL geändert"),
    LINK_REMOVED("Link entfernt"),

    // Response Header Details
    RESPONSE_HEADER_REQUIRED_ADDED("Response-Header wurde required"),

    // ========================================
    // WARNING (Deprecations)
    // ========================================

    OPERATION_DEPRECATED_ADDED("Operation als deprecated markiert"),
    PARAMETER_DEPRECATED_ADDED("Parameter als deprecated markiert"),
    SCHEMA_DEPRECATED_ADDED("Schema als deprecated markiert"),

    // ========================================
    // INFO / NON-BREAKING CHANGES
    // ========================================

    // Additions
    ENDPOINT_ADDED("Endpoint hinzugefügt"),
    METHOD_ADDED("HTTP-Methode hinzugefügt"),
    PARAMETER_ADDED("Parameter hinzugefügt"),
    PROPERTY_ADDED("Property hinzugefügt"),
    ENUM_VALUE_ADDED("Enum-Wert hinzugefügt"),

    // Request/Response Content
    REQUEST_BODY_ADDED("Request-Body hinzugefügt"),
    REQUEST_CONTENT_TYPE_ADDED("Request Content-Type hinzugefügt"),
    RESPONSE_CONTENT_TYPE_ADDED("Response Content-Type hinzugefügt"),

    // Headers
    RESPONSE_HEADER_ADDED("Response-Header hinzugefügt"),

    // Security (less restrictive)
    SECURITY_REQUIREMENT_REMOVED("Security-Anforderung entfernt"),

    // Callbacks & Links
    CALLBACK_ADDED("Callback hinzugefügt"),
    LINK_ADDED("Link hinzugefügt"),

    // Schema Composition
    ALL_OF_SCHEMA_CHANGED("AllOf-Schema geändert"),
    ANY_OF_ALL_OPTIONS_CHANGED("AnyOf-Optionen geändert");

    private final String description;

    ChangeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

