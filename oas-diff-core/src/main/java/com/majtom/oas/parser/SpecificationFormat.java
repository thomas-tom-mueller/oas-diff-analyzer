package com.majtom.oas.parser;
/**
 * Enum für unterstützte OAS-Dateiformate.
 */
public enum SpecificationFormat {
    /**
     * YAML-Format (.yaml, .yml)
     */
    YAML,
    /**
     * JSON-Format (.json)
     */
    JSON,
    /**
     * Format konnte nicht erkannt werden
     */
    UNKNOWN
}
