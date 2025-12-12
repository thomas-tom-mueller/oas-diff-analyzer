# OAS Diff Analyzer

Ein modulares Spring Boot Projekt zur Analyse und zum Vergleich von OpenAPI Specification (OAS) Dateien mit automatischer Breaking-Change-Erkennung.

## 🎯 Features

- **OAS-Parsing**: Unterstützt OpenAPI 3.0 in **YAML und JSON** Format
- **Automatische Format-Erkennung**: Erkennt automatisch YAML (.yaml, .yml) und JSON (.json) Dateien
- **Format-Konvertierung**: Interne Normalisierung zu JSON für konsistente Vergleiche
- **Versionsvergleich**: Vergleicht zwei OAS-Versionen und identifiziert alle Änderungen
- **Breaking-Change-Erkennung**: Automatische Erkennung von Breaking Changes mit Severity-Leveln
- **Menschenlesbare Berichte**: Formatierte Text- und HTML-Berichte
- **REST API**: Programmgesteuerte Nutzung über REST-Endpoints
- **Web-Interface**: Benutzerfreundliche HTML-Oberfläche
- **Modulare Architektur**: Klare Trennung in Core, Web und Examples Module

## 🏗️ Architektur

Das Projekt folgt einer modularen Multi-Modul-Maven-Struktur:

```
oas-diff-analyzer/
├── oas-diff-core/          # Kern-Logik
│   ├── model/              # Domain-Modelle
│   ├── parser/             # OAS-Parser
│   ├── analyzer/           # Vergleichs-Engine
│   ├── rules/              # Breaking-Change-Regeln
│   └── report/             # Report-Generatoren
├── oas-diff-web/           # Web-Interface & REST API
│   ├── controller/         # Spring MVC Controller
│   ├── dto/                # Data Transfer Objects
│   └── resources/
│       └── templates/      # Thymeleaf Templates
└── oas-diff-examples/      # Beispiel OAS-Dateien
    └── resources/examples/
```

## 🚀 Schnellstart

### Voraussetzungen

- Java 21 oder höher
- Maven 3.8+

### Build

```bash
cd oas-diff-analyzer
mvn clean install
```

### Anwendung starten

```bash
cd oas-diff-web
mvn spring-boot:run
```

Die Anwendung läuft auf: http://localhost:8080

## 📖 Verwendung

### Web-Interface

1. Öffne http://localhost:8080 im Browser
2. Gib die Pfade zu den beiden OAS-Dateien ein
3. Klicke auf "Vergleich starten"
4. Betrachte den detaillierten Bericht mit Breaking Changes

### REST API

#### Vergleich durchführen

```bash
curl -X POST "http://localhost:8080/api/oas/compare" \
  -d "oldSpecPath=/path/to/todo-api-v1.yaml" \
  -d "newSpecPath=/path/to/todo-api-v2.yaml"
```

#### Breaking Changes prüfen

```bash
curl "http://localhost:8080/api/oas/breaking-changes?oldSpecPath=/path/to/v1.yaml&newSpecPath=/path/to/v2.yaml"
```

#### Textbericht generieren

```bash
curl "http://localhost:8080/api/oas/report?oldSpecPath=/path/to/v1.yaml&newSpecPath=/path/to/v2.yaml"
```

## 🔍 Breaking Change Regeln

Das System erkennt folgende Breaking Changes:

### Kritisch (CRITICAL)
- ❌ Entfernte Endpoints
- ❌ Entfernte HTTP-Methoden
- ❌ Neue required Parameter
- ❌ Entfernte Success Response-Codes

### Wichtig (MAJOR)
- ⚠️ Parameter wurde von optional zu required
- ⚠️ Geänderte Success Response-Codes (z.B. 200 → 201)
- ⚠️ Neue required Properties in Request/Response

### Klein (MINOR)
- ℹ️ Entfernte Error Response-Codes
- ℹ️ Geänderte Property-Typen

## 📋 Beispiel-Dateien

Das Projekt enthält zwei Beispiel-OAS-Dateien für eine Todo-API:

- `oas-diff-examples/src/main/resources/examples/todo-api-v1.yaml` - Version 1.0.0
- `oas-diff-examples/src/main/resources/examples/todo-api-v2.yaml` - Version 2.0.0 (mit Breaking Changes)

### Breaking Changes in V2

Die V2 enthält folgende Breaking Changes zur Demonstration:

1. **DELETE /users/{userId}** wurde entfernt
2. **POST /users** Response-Code von 200 → 201 geändert
3. Neuer **required Parameter** `includeInactive` bei GET /users
4. Neues **required Feld** `accountType` im User-Schema
5. **priority** wurde zu required im TodoInput-Schema

## 🧪 Tests

```bash
# Alle Tests ausführen
mvn test

# Nur Core-Tests
cd oas-diff-core && mvn test
```

## 🛠️ Technologie-Stack

- **Spring Boot 3.2.0** - Application Framework
- **Java 21** - Programmiersprache
- **Swagger Parser 2.1.19** - OAS Parsing
- **Thymeleaf** - Template Engine
- **Maven** - Build Tool
- **JUnit 5** - Testing Framework

## 📦 Module Details

### oas-diff-core

Das Kern-Modul enthält die gesamte Business-Logik:

- **OasParser**: Parst OAS-Dateien (YAML/JSON)
- **OasComparisonService**: Orchestriert den Vergleich
- **BreakingChangeRule Interface**: Basis für alle Regeln
- **Regel-Implementierungen**: 
  - EndpointRemovedRule
  - MethodRemovedRule
  - RequiredParameterAddedRule
  - ResponseCodeChangedRule
- **HumanReadableReportGenerator**: Erstellt formatierte Berichte

### oas-diff-web

Das Web-Modul bietet die Benutzeroberfläche:

- **OasComparisonRestController**: REST API Endpoints
- **OasComparisonWebController**: HTML View Controller
- **DTOs**: API-Datenstrukturen
- **Thymeleaf Templates**: index.html, result.html

## 🔧 Konfiguration

Die Anwendung kann über `application.properties` konfiguriert werden:

```properties
# Server-Port
server.port=8080

# Logging-Level
logging.level.com.oas.diff=DEBUG

# File Upload Limits
spring.servlet.multipart.max-file-size=10MB
```

## 📝 Code-Qualität

Das Projekt folgt Best Practices:

- ✅ Klare Separation of Concerns
- ✅ Dependency Injection über Spring
- ✅ Builder Pattern für komplexe Objekte
- ✅ Immutable Domain Objects
- ✅ Ausführliche JavaDoc-Kommentare
- ✅ Exception Handling
- ✅ Logging mit SLF4J

## 🤝 Erweiterbarkeit

### Neue Breaking-Change-Regel hinzufügen

1. Erstelle eine Klasse in `com.oas.diff.rules`
2. Implementiere das `BreakingChangeRule` Interface
3. Annotiere mit `@Component`
4. Die Regel wird automatisch erkannt und angewendet

Beispiel:

```java
@Component
public class MyCustomRule implements BreakingChangeRule {
    @Override
    public List<ApiChange> evaluate(OpenAPI oldSpec, OpenAPI newSpec) {
        // Implementierung
    }
    
    @Override
    public String getRuleName() {
        return "My Custom Rule";
    }
}
```

## 📄 Lizenz

Dieses Projekt ist ein Demonstrationsprojekt für OAS-Versionsvergleich.

## 👥 Autor

Thomas Müller