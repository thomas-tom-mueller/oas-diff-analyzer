package com.oas.diff.parser;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integrationstests für OasParser mit YAML- und JSON-Unterstützung.
 */
@SpringBootTest(classes = {OasParser.class, FormatDetector.class, FormatConverter.class})
class OasParserFormatIntegrationTest {

    @Autowired
    private OasParser oasParser;

    @Test
    @DisplayName("Sollte YAML-Datei erfolgreich parsen")
    void shouldParseYamlFile() throws OasParseException {
        String yamlPath = "src/test/resources/test-api.yaml";
        createTestYamlFile(yamlPath);

        try {
            OpenAPI openAPI = oasParser.parseFromFile(yamlPath);

            assertNotNull(openAPI);
            assertNotNull(openAPI.getInfo());
            assertEquals("Test API", openAPI.getInfo().getTitle());
            assertEquals("1.0.0", openAPI.getInfo().getVersion());
        } finally {
            cleanupTestFile(yamlPath);
        }
    }

    @Test
    @DisplayName("Sollte JSON-Datei erfolgreich parsen")
    void shouldParseJsonFile() throws OasParseException {
        String jsonPath = "src/test/resources/test-api.json";
        createTestJsonFile(jsonPath);

        try {
            OpenAPI openAPI = oasParser.parseFromFile(jsonPath);

            assertNotNull(openAPI);
            assertNotNull(openAPI.getInfo());
            assertEquals("Test API", openAPI.getInfo().getTitle());
            assertEquals("1.0.0", openAPI.getInfo().getVersion());
        } finally {
            cleanupTestFile(jsonPath);
        }
    }

    @Test
    @DisplayName("Sollte YAML-String erfolgreich parsen")
    void shouldParseYamlString() throws OasParseException {
        String yamlContent = "openapi: 3.0.0\n" +
                "info:\n" +
                "  title: Test API\n" +
                "  version: 1.0.0\n" +
                "paths: {}";

        OpenAPI openAPI = oasParser.parseFromString(yamlContent);

        assertNotNull(openAPI);
        assertEquals("Test API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }

    @Test
    @DisplayName("Sollte JSON-String erfolgreich parsen")
    void shouldParseJsonString() throws OasParseException {
        String jsonContent = "{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"Test API\",\"version\":\"1.0.0\"},\"paths\":{}}";

        OpenAPI openAPI = oasParser.parseFromString(jsonContent);

        assertNotNull(openAPI);
        assertEquals("Test API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }

    @Test
    @DisplayName("Sollte FormatDetector und FormatConverter bereitstellen")
    void shouldProvideFormatDetectorAndConverter() {
        assertNotNull(oasParser.getFormatDetector());
        assertNotNull(oasParser.getFormatConverter());
    }

    @Test
    @DisplayName("Sollte Format korrekt erkennen")
    void shouldDetectFormatCorrectly() {
        FormatDetector detector = oasParser.getFormatDetector();

        assertEquals(SpecificationFormat.YAML, detector.detectFromFilePath("test.yaml"));
        assertEquals(SpecificationFormat.YAML, detector.detectFromFilePath("test.yml"));
        assertEquals(SpecificationFormat.JSON, detector.detectFromFilePath("test.json"));
    }

    // Helper-Methoden

    private void createTestYamlFile(String path) {
        try {
            String yamlContent = "openapi: 3.0.0\n" +
                    "info:\n" +
                    "  title: Test API\n" +
                    "  version: 1.0.0\n" +
                    "paths:\n" +
                    "  /test:\n" +
                    "    get:\n" +
                    "      responses:\n" +
                    "        '200':\n" +
                    "          description: Success";

            Path filePath = Path.of(path);
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, yamlContent);
        } catch (IOException e) {
            throw new RuntimeException("Konnte Test-YAML-Datei nicht erstellen", e);
        }
    }

    private void createTestJsonFile(String path) {
        try {
            String jsonContent = "{\n" +
                    "  \"openapi\": \"3.0.0\",\n" +
                    "  \"info\": {\n" +
                    "    \"title\": \"Test API\",\n" +
                    "    \"version\": \"1.0.0\"\n" +
                    "  },\n" +
                    "  \"paths\": {\n" +
                    "    \"/test\": {\n" +
                    "      \"get\": {\n" +
                    "        \"responses\": {\n" +
                    "          \"200\": {\n" +
                    "            \"description\": \"Success\"\n" +
                    "          }\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";

            Path filePath = Path.of(path);
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, jsonContent);
        } catch (IOException e) {
            throw new RuntimeException("Konnte Test-JSON-Datei nicht erstellen", e);
        }
    }

    private void cleanupTestFile(String path) {
        try {
            Files.deleteIfExists(Path.of(path));
        } catch (IOException e) {
            // Ignorieren
        }
    }
}

