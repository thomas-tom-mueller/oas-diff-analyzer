package com.majtom.oas.web.controller;

import com.majtom.oas.analyzer.OasComparisonService;
import com.majtom.oas.model.ComparisonResult;
import com.majtom.oas.parser.OasParseException;
import com.majtom.oas.report.HumanReadableReportGenerator;
import com.majtom.oas.web.dto.ComparisonResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST-Controller für OAS-Vergleich.
 */
@RestController
@RequestMapping("/api/oas")
public class OasComparisonRestController {

    private static final Logger log = LoggerFactory.getLogger(OasComparisonRestController.class);

    private final OasComparisonService comparisonService;
    private final HumanReadableReportGenerator reportGenerator;

    public OasComparisonRestController(OasComparisonService comparisonService,
                                       HumanReadableReportGenerator reportGenerator) {
        this.comparisonService = comparisonService;
        this.reportGenerator = reportGenerator;
    }

    /**
     * Vergleicht zwei OAS-Dateien und gibt das Ergebnis als JSON zurück.
     *
     * @param oldSpecPath Pfad zur alten OAS-Datei
     * @param newSpecPath Pfad zur neuen OAS-Datei
     * @return Vergleichsergebnis als JSON
     */
    @PostMapping("/compare")
    public ResponseEntity<ComparisonResultDto> compareSpecifications(
            @RequestParam String oldSpecPath,
            @RequestParam String newSpecPath) {

        try {
            log.info("REST API: Vergleiche {} mit {}", oldSpecPath, newSpecPath);

            ComparisonResult result = comparisonService.compareSpecifications(oldSpecPath, newSpecPath);
            ComparisonResultDto dto = ComparisonResultDto.fromModel(result);

            return ResponseEntity.ok(dto);

        } catch (OasParseException e) {
            log.error("Fehler beim Parsen der OAS-Dateien", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Überprüft, ob Breaking Changes vorhanden sind.
     *
     * @param oldSpecPath Pfad zur alten OAS-Datei
     * @param newSpecPath Pfad zur neuen OAS-Datei
     * @return Status mit hasBreakingChanges-Flag
     */
    @GetMapping("/breaking-changes")
    public ResponseEntity<Map<String, Object>> checkBreakingChanges(
            @RequestParam String oldSpecPath,
            @RequestParam String newSpecPath) {

        try {
            log.info("REST API: Prüfe Breaking Changes {} -> {}", oldSpecPath, newSpecPath);

            boolean hasBreakingChanges = comparisonService.hasBreakingChanges(oldSpecPath, newSpecPath);

            Map<String, Object> response = new HashMap<>();
            response.put("hasBreakingChanges", hasBreakingChanges);
            response.put("oldSpecPath", oldSpecPath);
            response.put("newSpecPath", newSpecPath);

            return ResponseEntity.ok(response);

        } catch (OasParseException e) {
            log.error("Fehler beim Parsen der OAS-Dateien", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Erstellt einen menschenlesbaren Textbericht.
     *
     * @param oldSpecPath Pfad zur alten OAS-Datei
     * @param newSpecPath Pfad zur neuen OAS-Datei
     * @return Textbericht
     */
    @GetMapping(value = "/report", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> generateTextReport(
            @RequestParam String oldSpecPath,
            @RequestParam String newSpecPath) {

        try {
            log.info("REST API: Generiere Textbericht {} -> {}", oldSpecPath, newSpecPath);

            ComparisonResult result = comparisonService.compareSpecifications(oldSpecPath, newSpecPath);
            String report = reportGenerator.generateTextReport(result);

            return ResponseEntity.ok(report);

        } catch (OasParseException e) {
            log.error("Fehler beim Parsen der OAS-Dateien", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Fehler: " + e.getMessage());
        }
    }

    /**
     * Exception Handler für allgemeine Fehler.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Unerwarteter Fehler", e);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Interner Server-Fehler");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

