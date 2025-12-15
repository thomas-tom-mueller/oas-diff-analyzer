package com.majtom.oas.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests für FormatDetector.
 */
class FormatDetectorTest {

    private final FormatDetector formatDetector = new FormatDetector();

    @Test
    @DisplayName("Sollte YAML-Format anhand .yaml Endung erkennen")
    void shouldDetectYamlFromYamlExtension() {
        String filePath = "/path/to/api-spec.yaml";
        SpecificationFormat format = formatDetector.detectFromFilePath(filePath);
        assertEquals(SpecificationFormat.YAML, format);
    }

    @Test
    @DisplayName("Sollte YAML-Format anhand .yml Endung erkennen")
    void shouldDetectYamlFromYmlExtension() {
        String filePath = "/path/to/api-spec.yml";
        SpecificationFormat format = formatDetector.detectFromFilePath(filePath);
        assertEquals(SpecificationFormat.YAML, format);
    }

    @Test
    @DisplayName("Sollte JSON-Format anhand .json Endung erkennen")
    void shouldDetectJsonFromJsonExtension() {
        String filePath = "/path/to/api-spec.json";
        SpecificationFormat format = formatDetector.detectFromFilePath(filePath);
        assertEquals(SpecificationFormat.JSON, format);
    }

    @Test
    @DisplayName("Sollte UNKNOWN für unbekannte Endung zurückgeben")
    void shouldReturnUnknownForUnknownExtension() {
        String filePath = "/path/to/api-spec.txt";
        SpecificationFormat format = formatDetector.detectFromFilePath(filePath);
        assertEquals(SpecificationFormat.UNKNOWN, format);
    }

    @Test
    @DisplayName("Sollte JSON-Format anhand Content erkennen")
    void shouldDetectJsonFromContent() {
        String jsonContent = "{\"openapi\": \"3.0.0\"}";
        SpecificationFormat format = formatDetector.detectFromContent(jsonContent);
        assertEquals(SpecificationFormat.JSON, format);
    }

    @Test
    @DisplayName("Sollte YAML-Format anhand Content erkennen")
    void shouldDetectYamlFromContent() {
        String yamlContent = "openapi: 3.0.0\ninfo:\n  title: Test API";
        SpecificationFormat format = formatDetector.detectFromContent(yamlContent);
        assertEquals(SpecificationFormat.YAML, format);
    }

    @Test
    @DisplayName("Sollte UNKNOWN für leeren Content zurückgeben")
    void shouldReturnUnknownForEmptyContent() {
        String emptyContent = "";
        SpecificationFormat format = formatDetector.detectFromContent(emptyContent);
        assertEquals(SpecificationFormat.UNKNOWN, format);
    }

    @Test
    @DisplayName("Sollte UNKNOWN für null Content zurückgeben")
    void shouldReturnUnknownForNullContent() {
        SpecificationFormat format = formatDetector.detectFromContent(null);
        assertEquals(SpecificationFormat.UNKNOWN, format);
    }

    @Test
    @DisplayName("Sollte case-insensitive bei Dateiendungen sein")
    void shouldBeCaseInsensitiveForFileExtensions() {
        assertEquals(SpecificationFormat.YAML, formatDetector.detectFromFilePath("/path/spec.YAML"));
        assertEquals(SpecificationFormat.YAML, formatDetector.detectFromFilePath("/path/spec.YML"));
        assertEquals(SpecificationFormat.JSON, formatDetector.detectFromFilePath("/path/spec.JSON"));
    }
}

