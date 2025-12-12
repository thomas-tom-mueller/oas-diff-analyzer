package com.oas.diff.rules;

import com.oas.diff.model.ApiChange;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.List;

/**
 * Interface für Breaking-Change-Regeln.
 * Jede Regel überprüft einen spezifischen Aspekt der API-Änderung.
 */
public interface BreakingChangeRule {

    /**
     * Überprüft, ob die Änderung von oldSpec zu newSpec einen Breaking Change darstellt.
     *
     * @param oldSpec Alte OpenAPI-Spezifikation
     * @param newSpec Neue OpenAPI-Spezifikation
     * @return Liste der gefundenen Änderungen
     */
    List<ApiChange> evaluate(OpenAPI oldSpec, OpenAPI newSpec);

    /**
     * Name der Regel für Logging und Reporting.
     *
     * @return Regelname
     */
    String getRuleName();
}

