package com.majtom.oas.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Service zur Erkennung des Dateiformats (YAML oder JSON) von OAS-Spezifikationen.
 */
@Component
public class FormatDetector {

    private static final Logger log = LoggerFactory.getLogger(FormatDetector.class);

    /**
     * Erkennt das Format anhand der Dateiendung.
     *
     * @param filePath Pfad zur Datei
     * @return Erkanntes Format
     */
    public SpecificationFormat detectFromFilePath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            log.warn("Leerer Dateipfad, Format kann nicht erkannt werden");
            return SpecificationFormat.UNKNOWN;
        }

        String lowerCasePath = filePath.toLowerCase();

        if (lowerCasePath.endsWith(".yaml") || lowerCasePath.endsWith(".yml")) {
            log.debug("YAML-Format erkannt für: {}", filePath);
            return SpecificationFormat.YAML;
        }

        if (lowerCasePath.endsWith(".json")) {
            log.debug("JSON-Format erkannt für: {}", filePath);
            return SpecificationFormat.JSON;
        }

        log.warn("Unbekanntes Dateiformat für: {}", filePath);
        return SpecificationFormat.UNKNOWN;
    }

    /**
     * Erkennt das Format anhand des Inhalts (erste Zeichen).
     *
     * @param content Dateiinhalt
     * @return Erkanntes Format
     */
    public SpecificationFormat detectFromContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            log.warn("Leerer Content, Format kann nicht erkannt werden");
            return SpecificationFormat.UNKNOWN;
        }

        String trimmedContent = content.trim();

        // JSON beginnt typischerweise mit {
        if (trimmedContent.startsWith("{")) {
            log.debug("JSON-Format anhand Content erkannt");
            return SpecificationFormat.JSON;
        }

        // YAML beginnt oft mit openapi: oder hat andere typische Kennzeichen
        if (trimmedContent.startsWith("openapi:") ||
            trimmedContent.startsWith("swagger:") ||
            trimmedContent.matches("(?s)^[a-zA-Z][a-zA-Z0-9_-]*:\\s.*")) {
            log.debug("YAML-Format anhand Content erkannt");
            return SpecificationFormat.YAML;
        }

        log.warn("Format konnte nicht anhand Content erkannt werden");
        return SpecificationFormat.UNKNOWN;
    }
}

