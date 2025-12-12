package com.oas.diff.web.controller;

import com.oas.diff.analyzer.OasComparisonService;
import com.oas.diff.model.ComparisonResult;
import com.oas.diff.parser.OasParseException;
import com.oas.diff.report.HumanReadableReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Web-Controller für HTML-Ansichten.
 */
@Controller
public class OasComparisonWebController {

    private static final Logger log = LoggerFactory.getLogger(OasComparisonWebController.class);

    private final OasComparisonService comparisonService;
    private final HumanReadableReportGenerator reportGenerator;

    public OasComparisonWebController(OasComparisonService comparisonService,
                                      HumanReadableReportGenerator reportGenerator) {
        this.comparisonService = comparisonService;
        this.reportGenerator = reportGenerator;
    }

    /**
     * Zeigt die Startseite mit dem Formular an.
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Verarbeitet den Vergleich und zeigt das Ergebnis an.
     */
    @PostMapping("/compare")
    public String compare(@RequestParam String oldSpecPath,
                         @RequestParam String newSpecPath,
                         Model model) {

        try {
            log.info("Web: Vergleiche {} mit {}", oldSpecPath, newSpecPath);

            ComparisonResult result = comparisonService.compareSpecifications(oldSpecPath, newSpecPath);
            String report = reportGenerator.generateTextReport(result);

            model.addAttribute("result", result);
            model.addAttribute("report", report);
            model.addAttribute("oldSpecPath", oldSpecPath);
            model.addAttribute("newSpecPath", newSpecPath);

            return "result";

        } catch (OasParseException e) {
            log.error("Fehler beim Vergleich", e);
            model.addAttribute("error", "Fehler beim Parsen der OAS-Dateien: " + e.getMessage());
            return "index";
        }
    }
}

