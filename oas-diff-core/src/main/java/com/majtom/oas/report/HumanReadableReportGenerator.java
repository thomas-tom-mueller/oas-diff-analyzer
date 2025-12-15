package com.majtom.oas.report;

import com.majtom.oas.model.ApiChange;
import com.majtom.oas.model.ComparisonResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Erstellt menschenlesbare Berichte aus Vergleichsergebnissen.
 */
@Component
public class HumanReadableReportGenerator {

    /**
     * Generiert einen formatierten Textbericht.
     *
     * @param result Vergleichsergebnis
     * @return Formatierter Bericht als String
     */
    public String generateTextReport(ComparisonResult result) {
        StringBuilder report = new StringBuilder();

        report.append("═══════════════════════════════════════════════════════════════\n");
        report.append("       OAS VERSIONSVERGLEICH - ÄNDERUNGSBERICHT\n");
        report.append("═══════════════════════════════════════════════════════════════\n\n");

        report.append(String.format("Alte Version:  %s%n", result.getOldVersion()));
        report.append(String.format("Neue Version:  %s%n", result.getNewVersion()));
        report.append(String.format("Zeitstempel:   %s%n", new java.util.Date(result.getTimestamp())));
        report.append("\n");

        report.append("───────────────────────────────────────────────────────────────\n");
        report.append("  ZUSAMMENFASSUNG\n");
        report.append("───────────────────────────────────────────────────────────────\n");
        report.append(String.format("Änderungen gesamt:          %d%n", result.getTotalChangesCount()));
        report.append(String.format("Breaking Changes:           %d%n", result.getBreakingChangesCount()));
        report.append(String.format("Nicht-Breaking Changes:     %d%n",
                result.getTotalChangesCount() - result.getBreakingChangesCount()));
        report.append("\n");

        if (result.hasBreakingChanges()) {
            report.append("⚠️  WARNUNG: Diese Version enthält BREAKING CHANGES!\n\n");
        } else {
            report.append("✓ Diese Version ist abwärtskompatibel (keine Breaking Changes).\n\n");
        }

        // Breaking Changes
        List<ApiChange> breakingChanges = result.getBreakingChanges();
        if (!breakingChanges.isEmpty()) {
            report.append("═══════════════════════════════════════════════════════════════\n");
            report.append("  BREAKING CHANGES\n");
            report.append("═══════════════════════════════════════════════════════════════\n\n");

            Map<String, List<ApiChange>> groupedByPath = breakingChanges.stream()
                    .collect(Collectors.groupingBy(ApiChange::getPath));

            int changeNumber = 1;
            for (Map.Entry<String, List<ApiChange>> entry : groupedByPath.entrySet()) {
                for (ApiChange change : entry.getValue()) {
                    report.append(String.format("%d. %s%n", changeNumber++, formatChange(change)));
                }
            }
            report.append("\n");
        }

        // Nicht-Breaking Changes
        List<ApiChange> nonBreakingChanges = result.getNonBreakingChanges();
        if (!nonBreakingChanges.isEmpty()) {
            report.append("═══════════════════════════════════════════════════════════════\n");
            report.append("  WEITERE ÄNDERUNGEN (Nicht-Breaking)\n");
            report.append("═══════════════════════════════════════════════════════════════\n\n");

            Map<String, List<ApiChange>> groupedByPath = nonBreakingChanges.stream()
                    .collect(Collectors.groupingBy(ApiChange::getPath));

            int changeNumber = 1;
            for (Map.Entry<String, List<ApiChange>> entry : groupedByPath.entrySet()) {
                for (ApiChange change : entry.getValue()) {
                    report.append(String.format("%d. %s%n", changeNumber++, formatChange(change)));
                }
            }
            report.append("\n");
        }

        report.append("═══════════════════════════════════════════════════════════════\n");
        report.append("  ENDE DES BERICHTS\n");
        report.append("═══════════════════════════════════════════════════════════════\n");

        return report.toString();
    }

    private String formatChange(ApiChange change) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("[%s] ", change.getSeverity().getDisplayName()));
        sb.append(change.getPath());
        sb.append("\n   ");
        sb.append(change.getType().getDescription());
        sb.append(": ");
        sb.append(change.getDescription());

        if (change.getOldValue() != null || change.getNewValue() != null) {
            sb.append("\n   ");
            if (change.getOldValue() != null) {
                sb.append("Alt: ").append(change.getOldValue());
            }
            if (change.getNewValue() != null) {
                if (change.getOldValue() != null) {
                    sb.append(" → ");
                }
                sb.append("Neu: ").append(change.getNewValue());
            }
        }

        sb.append("\n");

        return sb.toString();
    }

    /**
     * Generiert einen kompakten Einzeiler-Bericht.
     *
     * @param result Vergleichsergebnis
     * @return Kompakter Bericht
     */
    public String generateCompactReport(ComparisonResult result) {
        return result.getSummary();
    }
}

