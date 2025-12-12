package com.oas.diff.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Service zur Konvertierung zwischen YAML und JSON für OAS-Spezifikationen.
 * Alle Vergleiche werden intern auf JSON-Basis durchgeführt.
 */
@Component
public class FormatConverter {

    private static final Logger log = LoggerFactory.getLogger(FormatConverter.class);

    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;

    public FormatConverter() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.jsonMapper = new ObjectMapper();
    }

    /**
     * Konvertiert YAML-Content zu JSON-String.
     *
     * @param yamlContent YAML-Inhalt als String
     * @return JSON-String
     * @throws OasParseException bei Konvertierungsfehlern
     */
    public String yamlToJson(String yamlContent) throws OasParseException {
        try {
            log.debug("Konvertiere YAML zu JSON");

            // YAML in JsonNode parsen
            JsonNode jsonNode = yamlMapper.readTree(yamlContent);

            // JsonNode zu JSON-String konvertieren
            String jsonString = jsonMapper.writeValueAsString(jsonNode);

            log.debug("YAML erfolgreich zu JSON konvertiert");
            return jsonString;

        } catch (Exception e) {
            throw new OasParseException("Fehler bei der Konvertierung von YAML zu JSON", e);
        }
    }

    /**
     * Konvertiert JSON-Content zu YAML-String.
     *
     * @param jsonContent JSON-Inhalt als String
     * @return YAML-String
     * @throws OasParseException bei Konvertierungsfehlern
     */
    public String jsonToYaml(String jsonContent) throws OasParseException {
        try {
            log.debug("Konvertiere JSON zu YAML");

            // JSON in JsonNode parsen
            JsonNode jsonNode = jsonMapper.readTree(jsonContent);

            // JsonNode zu YAML-String konvertieren
            String yamlString = yamlMapper.writeValueAsString(jsonNode);

            log.debug("JSON erfolgreich zu YAML konvertiert");
            return yamlString;

        } catch (Exception e) {
            throw new OasParseException("Fehler bei der Konvertierung von JSON zu YAML", e);
        }
    }

    /**
     * Normalisiert den Content zu JSON, unabhängig vom Eingabeformat.
     *
     * @param content OAS-Content (YAML oder JSON)
     * @param format Format des Contents
     * @return JSON-String
     * @throws OasParseException bei Konvertierungsfehlern
     */
    public String normalizeToJson(String content, SpecificationFormat format) throws OasParseException {
        if (content == null || content.trim().isEmpty()) {
            throw new OasParseException("Content darf nicht leer sein");
        }

        switch (format) {
            case JSON:
                log.debug("Content ist bereits JSON, keine Konvertierung nötig");
                return content;

            case YAML:
                log.debug("Konvertiere YAML-Content zu JSON für Vergleich");
                return yamlToJson(content);

            default:
                throw new OasParseException("Unbekanntes Format: " + format);
        }
    }

    /**
     * Parst Content zu JsonNode, unabhängig vom Format.
     *
     * @param content OAS-Content (YAML oder JSON)
     * @param format Format des Contents
     * @return JsonNode
     * @throws OasParseException bei Parse-Fehlern
     */
    public JsonNode parseToJsonNode(String content, SpecificationFormat format) throws OasParseException {
        try {
            switch (format) {
                case JSON:
                    log.debug("Parse JSON-Content zu JsonNode");
                    return jsonMapper.readTree(content);

                case YAML:
                    log.debug("Parse YAML-Content zu JsonNode");
                    return yamlMapper.readTree(content);

                default:
                    throw new OasParseException("Unbekanntes Format: " + format);
            }
        } catch (Exception e) {
            throw new OasParseException("Fehler beim Parsen zu JsonNode", e);
        }
    }
}

