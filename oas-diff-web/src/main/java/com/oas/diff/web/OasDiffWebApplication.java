package com.oas.diff.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hauptklasse der Spring Boot Anwendung für OAS-Diff-Analyzer.
 */
@SpringBootApplication(scanBasePackages = "com.oas.diff")
public class OasDiffWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(OasDiffWebApplication.class, args);
    }
}

