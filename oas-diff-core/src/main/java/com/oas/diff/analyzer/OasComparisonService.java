package com.oas.diff.analyzer;

import com.oas.diff.model.ApiChange;
import com.oas.diff.model.ComparisonResult;
import com.oas.diff.parser.OasParseException;
import com.oas.diff.parser.OasParser;
import com.oas.diff.rules.BreakingChangeRule;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service für den Vergleich von OpenAPI-Spezifikationen.
 */
@Service
public class OasComparisonService {

    private static final Logger log = LoggerFactory.getLogger(OasComparisonService.class);

    private final OasParser parser;
    private final List<BreakingChangeRule> rules;

    public OasComparisonService(OasParser parser, List<BreakingChangeRule> rules) {
        this.parser = parser;
        this.rules = rules;
        log.info("OasComparisonService initialisiert mit {} Regeln", rules.size());
    }

    /**
     * Vergleicht zwei OAS-Dateien und erkennt alle Änderungen inklusive Breaking Changes.
     *
     * @param oldSpecPath Pfad zur alten OAS-Datei
     * @param newSpecPath Pfad zur neuen OAS-Datei
     * @return Vergleichsergebnis mit allen Änderungen
     * @throws OasParseException wenn das Parsen fehlschlägt
     */
    public ComparisonResult compareSpecifications(String oldSpecPath, String newSpecPath) throws OasParseException {
        log.info("Starte Vergleich: {} -> {}", oldSpecPath, newSpecPath);

        OpenAPI oldSpec = parser.parseFromFile(oldSpecPath);
        OpenAPI newSpec = parser.parseFromFile(newSpecPath);

        return compareSpecifications(oldSpec, newSpec);
    }

    /**
     * Vergleicht zwei geparste OpenAPI-Spezifikationen.
     *
     * @param oldSpec Alte OpenAPI-Spezifikation
     * @param newSpec Neue OpenAPI-Spezifikation
     * @return Vergleichsergebnis mit allen Änderungen
     */
    public ComparisonResult compareSpecifications(OpenAPI oldSpec, OpenAPI newSpec) {
        String oldVersion = parser.extractVersion(oldSpec);
        String newVersion = parser.extractVersion(newSpec);

        log.info("Vergleiche Versionen: {} -> {}", oldVersion, newVersion);

        List<ApiChange> allChanges = new ArrayList<>();

        for (BreakingChangeRule rule : rules) {
            log.debug("Wende Regel an: {}", rule.getRuleName());
            List<ApiChange> changes = rule.evaluate(oldSpec, newSpec);
            allChanges.addAll(changes);
            log.debug("Regel {} fand {} Änderungen", rule.getRuleName(), changes.size());
        }

        ComparisonResult result = new ComparisonResult(oldVersion, newVersion, allChanges);

        log.info("Vergleich abgeschlossen: {}", result.getSummary());

        return result;
    }

    /**
     * Überprüft, ob die neue Version Breaking Changes enthält.
     *
     * @param oldSpecPath Pfad zur alten OAS-Datei
     * @param newSpecPath Pfad zur neuen OAS-Datei
     * @return true wenn Breaking Changes gefunden wurden
     * @throws OasParseException wenn das Parsen fehlschlägt
     */
    public boolean hasBreakingChanges(String oldSpecPath, String newSpecPath) throws OasParseException {
        ComparisonResult result = compareSpecifications(oldSpecPath, newSpecPath);
        return result.hasBreakingChanges();
    }

    /**
     * Gibt nur die Breaking Changes zurück.
     *
     * @param oldSpecPath Pfad zur alten OAS-Datei
     * @param newSpecPath Pfad zur neuen OAS-Datei
     * @return Liste der Breaking Changes
     * @throws OasParseException wenn das Parsen fehlschlägt
     */
    public List<ApiChange> getBreakingChanges(String oldSpecPath, String newSpecPath) throws OasParseException {
        ComparisonResult result = compareSpecifications(oldSpecPath, newSpecPath);
        return result.getBreakingChanges();
    }
}

