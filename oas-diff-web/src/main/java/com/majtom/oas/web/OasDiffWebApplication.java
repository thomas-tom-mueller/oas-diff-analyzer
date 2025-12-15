package com.majtom.oas.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hauptklasse der Spring Boot Anwendung für OAS-Diff-Analyzer.
 */
@SpringBootApplication(scanBasePackages = "com.majtom.oas")
public class OasDiffWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(OasDiffWebApplication.class, args);
    }
}

