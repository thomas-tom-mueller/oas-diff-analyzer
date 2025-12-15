package com.majtom.oas.parser;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service zum Parsen von OpenAPI Specification Dateien.
 * Unterstützt sowohl YAML als auch JSON Format.
 * Alle Vergleiche werden intern auf JSON-Basis durchgeführt.
 */
@Component
public class OasParser {

    private static final Logger log = LoggerFactory.getLogger(OasParser.class);

    private final OpenAPIV3Parser parser;
    private final ParseOptions parseOptions;
    private final FormatDetector formatDetector;
    private final FormatConverter formatConverter;

    public OasParser(FormatDetector formatDetector, FormatConverter formatConverter) {
        this.parser = new OpenAPIV3Parser();
        this.parseOptions = new ParseOptions();
        this.parseOptions.setResolve(true);
        this.parseOptions.setResolveFully(true);
        this.formatDetector = formatDetector;
        this.formatConverter = formatConverter;
    }

    /**
     * Parst eine OAS-Datei von einem Dateipfad.
     * Unterstützt sowohl YAML (.yaml, .yml) als auch JSON (.json) Formate.
     *
     * @param filePath Pfad zur OAS-Datei (YAML oder JSON)
     * @return Geparste OpenAPI-Spezifikation
     * @throws OasParseException wenn das Parsen fehlschlägt
     */
    public OpenAPI parseFromFile(String filePath) throws OasParseException {
        try {
            // Format erkennen
            SpecificationFormat format = formatDetector.detectFromFilePath(filePath);
            log.info("Parse OAS-Datei: {} (Format: {})", filePath, format);

            if (!Files.exists(Path.of(filePath))) {
                throw new OasParseException("Datei nicht gefunden: " + filePath);
            }

            // Swagger Parser unterstützt beide Formate nativ
            SwaggerParseResult result = parser.readLocation(filePath, null, parseOptions);

            if (result.getMessages() != null && !result.getMessages().isEmpty()) {
                log.warn("Parse-Warnungen für {}: {}", filePath, result.getMessages());
            }

            OpenAPI openAPI = result.getOpenAPI();
            if (openAPI == null) {
                throw new OasParseException("Konnte OAS-Datei nicht parsen: " + filePath +
                        ". Fehler: " + result.getMessages());
            }

            log.info("OAS-Datei erfolgreich geparst: {} (Version: {}, Format: {})",
                    filePath,
                    openAPI.getInfo() != null ? openAPI.getInfo().getVersion() : "unbekannt",
                    format);

            return openAPI;

        } catch (Exception e) {
            throw new OasParseException("Fehler beim Parsen der OAS-Datei: " + filePath, e);
        }
    }

    /**
     * Parst eine OAS-Spezifikation aus einem String (YAML oder JSON).
     * Das Format wird automatisch erkannt.
     *
     * @param content OAS-Inhalt als String
     * @return Geparste OpenAPI-Spezifikation
     * @throws OasParseException wenn das Parsen fehlschlägt
     */
    public OpenAPI parseFromString(String content) throws OasParseException {
        try {
            // Format aus Content erkennen
            SpecificationFormat format = formatDetector.detectFromContent(content);
            log.debug("Parse OAS aus String-Content (Format: {})", format);

            SwaggerParseResult result = parser.readContents(content, null, parseOptions);

            if (result.getMessages() != null && !result.getMessages().isEmpty()) {
                log.warn("Parse-Warnungen: {}", result.getMessages());
            }

            OpenAPI openAPI = result.getOpenAPI();
            if (openAPI == null) {
                throw new OasParseException("Konnte OAS-Content nicht parsen. Fehler: " + result.getMessages());
            }

            log.debug("OAS-Content erfolgreich geparst (Format: {})", format);
            return openAPI;

        } catch (Exception e) {
            throw new OasParseException("Fehler beim Parsen des OAS-Contents", e);
        }
    }

    /**
     * Extrahiert die Version aus einer OpenAPI-Spezifikation.
     *
     * @param openAPI OpenAPI-Spezifikation
     * @return Version-String oder "unbekannt"
     */
    public String extractVersion(OpenAPI openAPI) {
        if (openAPI == null || openAPI.getInfo() == null) {
            return "unbekannt";
        }
        String version = openAPI.getInfo().getVersion();
        return version != null ? version : "unbekannt";
    }

    /**
     * Gibt den FormatConverter zurück für erweiterte Konvertierungen.
     *
     * @return FormatConverter-Instanz
     */
    public FormatConverter getFormatConverter() {
        return formatConverter;
    }

    /**
     * Gibt den FormatDetector zurück für Format-Erkennungen.
     *
     * @return FormatDetector-Instanz
     */
    public FormatDetector getFormatDetector() {
        return formatDetector;
    }
}

