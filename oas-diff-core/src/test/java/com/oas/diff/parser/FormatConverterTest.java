package com.oas.diff.parser;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für FormatConverter.
 */
class FormatConverterTest {

    private final FormatConverter formatConverter = new FormatConverter();

    @Test
    @DisplayName("Sollte YAML zu JSON konvertieren")
    void shouldConvertYamlToJson() throws OasParseException {
        String yamlContent = "openapi: 3.0.0\n" +
                "info:\n" +
                "  title: Test API\n" +
                "  version: 1.0.0";

        String jsonResult = formatConverter.yamlToJson(yamlContent);

        assertNotNull(jsonResult);
        assertTrue(jsonResult.contains("\"openapi\""));
        assertTrue(jsonResult.contains("\"3.0.0\""));
        assertTrue(jsonResult.contains("\"title\""));
        assertTrue(jsonResult.contains("\"Test API\""));
    }

    @Test
    @DisplayName("Sollte JSON zu YAML konvertieren")
    void shouldConvertJsonToYaml() throws OasParseException {
        String jsonContent = "{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"Test API\",\"version\":\"1.0.0\"}}";

        String yamlResult = formatConverter.jsonToYaml(jsonContent);

        assertNotNull(yamlResult);
        assertTrue(yamlResult.contains("openapi:"));
        assertTrue(yamlResult.contains("3.0.0"));
        assertTrue(yamlResult.contains("title:"));
        assertTrue(yamlResult.contains("Test API"));
    }

    @Test
    @DisplayName("Sollte JSON-Content normalisieren (ohne Änderung)")
    void shouldNormalizeJsonContentWithoutChange() throws OasParseException {
        String jsonContent = "{\"openapi\":\"3.0.0\"}";

        String normalized = formatConverter.normalizeToJson(jsonContent, SpecificationFormat.JSON);

        assertNotNull(normalized);
        assertEquals(jsonContent, normalized);
    }

    @Test
    @DisplayName("Sollte YAML-Content zu JSON normalisieren")
    void shouldNormalizeYamlContentToJson() throws OasParseException {
        String yamlContent = "openapi: 3.0.0";

        String normalized = formatConverter.normalizeToJson(yamlContent, SpecificationFormat.YAML);

        assertNotNull(normalized);
        assertTrue(normalized.contains("\"openapi\""));
        assertTrue(normalized.contains("\"3.0.0\""));
    }

    @Test
    @DisplayName("Sollte Exception werfen bei leerem Content")
    void shouldThrowExceptionForEmptyContent() {
        assertThrows(OasParseException.class, () -> {
            formatConverter.normalizeToJson("", SpecificationFormat.JSON);
        });
    }

    @Test
    @DisplayName("Sollte Exception werfen bei unbekanntem Format")
    void shouldThrowExceptionForUnknownFormat() {
        assertThrows(OasParseException.class, () -> {
            formatConverter.normalizeToJson("{\"test\":\"value\"}", SpecificationFormat.UNKNOWN);
        });
    }

    @Test
    @DisplayName("Sollte JSON zu JsonNode parsen")
    void shouldParseJsonToJsonNode() throws OasParseException {
        String jsonContent = "{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"Test\"}}";

        JsonNode jsonNode = formatConverter.parseToJsonNode(jsonContent, SpecificationFormat.JSON);

        assertNotNull(jsonNode);
        assertEquals("3.0.0", jsonNode.get("openapi").asText());
        assertEquals("Test", jsonNode.get("info").get("title").asText());
    }

    @Test
    @DisplayName("Sollte YAML zu JsonNode parsen")
    void shouldParseYamlToJsonNode() throws OasParseException {
        String yamlContent = "openapi: 3.0.0\ninfo:\n  title: Test";

        JsonNode jsonNode = formatConverter.parseToJsonNode(yamlContent, SpecificationFormat.YAML);

        assertNotNull(jsonNode);
        assertEquals("3.0.0", jsonNode.get("openapi").asText());
        assertEquals("Test", jsonNode.get("info").get("title").asText());
    }

    @Test
    @DisplayName("Sollte komplexe YAML-Strukturen korrekt konvertieren")
    void shouldConvertComplexYamlStructures() throws OasParseException {
        String yamlContent = "openapi: 3.0.0\n" +
                "paths:\n" +
                "  /users:\n" +
                "    get:\n" +
                "      summary: Get users\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: Success";

        String jsonResult = formatConverter.yamlToJson(yamlContent);

        assertNotNull(jsonResult);
        assertTrue(jsonResult.contains("\"paths\""));
        assertTrue(jsonResult.contains("\"/users\""));
        assertTrue(jsonResult.contains("\"get\""));
        assertTrue(jsonResult.contains("\"summary\""));
        assertTrue(jsonResult.contains("\"Get users\""));
    }

    @Test
    @DisplayName("Sollte Exception werfen bei ungültigem YAML")
    void shouldThrowExceptionForInvalidYaml() {
        String invalidYaml = "openapi: 3.0.0\n  invalid: : yaml";

        assertThrows(OasParseException.class, () -> {
            formatConverter.yamlToJson(invalidYaml);
        });
    }

    @Test
    @DisplayName("Sollte Exception werfen bei ungültigem JSON")
    void shouldThrowExceptionForInvalidJson() {
        String invalidJson = "{\"openapi\": \"3.0.0\"";

        assertThrows(OasParseException.class, () -> {
            formatConverter.jsonToYaml(invalidJson);
        });
    }
}

