# Format-Unterstützung: YAML und JSON

Der OAS Diff Analyzer unterstützt sowohl YAML- als auch JSON-Formate für OpenAPI-Spezifikationen. Die Implementierung ist modular, erweiterbar und nutzt etablierte Bibliotheken für maximale Kompatibilität.

## 📋 Übersicht

### Unterstützte Formate

| Format | Dateiendungen | Beschreibung |
|--------|---------------|--------------|
| **YAML** | `.yaml`, `.yml` | Standard-Format für OpenAPI, menschenlesbar |
| **JSON** | `.json` | Maschinenlesbares Format, oft von Tools generiert |

### Automatische Format-Erkennung

Die Anwendung erkennt das Format automatisch anhand:
1. **Dateiendung** (bei Dateipfaden)
2. **Content-Analyse** (bei String-Input)

## 🏗️ Architektur

Die Format-Unterstützung wurde durch drei neue Komponenten implementiert:

### 1. SpecificationFormat (Enum)

Definiert die unterstützten Formate:

```java
public enum SpecificationFormat {
    YAML,    // YAML-Format (.yaml, .yml)
    JSON,    // JSON-Format (.json)
    UNKNOWN  // Format konnte nicht erkannt werden
}
```

### 2. FormatDetector (Service)

Erkennt das Format von OAS-Dateien:

```java
@Component
public class FormatDetector {
    
    // Erkennt Format anhand Dateiendung
    public SpecificationFormat detectFromFilePath(String filePath)
    
    // Erkennt Format anhand Content
    public SpecificationFormat detectFromContent(String content)
}
```

**Erkennungslogik:**
- **Dateiendung**: `.yaml`, `.yml` → YAML; `.json` → JSON
- **Content**: Beginnt mit `{` → JSON; beginnt mit `openapi:` → YAML
- **Case-insensitive** bei Dateiendungen

### 3. FormatConverter (Service)

Konvertiert zwischen YAML und JSON:

```java
@Component
public class FormatConverter {
    
    // YAML zu JSON konvertieren
    public String yamlToJson(String yamlContent)
    
    // JSON zu YAML konvertieren
    public String jsonToYaml(String jsonContent)
    
    // Zu JSON normalisieren (für Vergleiche)
    public String normalizeToJson(String content, SpecificationFormat format)
    
    // Zu JsonNode parsen
    public JsonNode parseToJsonNode(String content, SpecificationFormat format)
}
```

**Verwendete Bibliotheken:**
- **Jackson Databind**: JSON-Verarbeitung
- **Jackson YAML**: YAML-Verarbeitung
- **Swagger Parser**: OAS-spezifisches Parsing

### 4. OasParser (Erweitert)

Der `OasParser` wurde erweitert, um die neuen Komponenten zu nutzen:

```java
@Component
public class OasParser {
    private final FormatDetector formatDetector;
    private final FormatConverter formatConverter;
    
    // Konstruktor mit Dependency Injection
    public OasParser(FormatDetector formatDetector, 
                     FormatConverter formatConverter) {
        // ...
    }
    
    // Parst Dateien (YAML oder JSON)
    public OpenAPI parseFromFile(String filePath)
    
    // Parst String-Content (YAML oder JSON)
    public OpenAPI parseFromString(String content)
}
```

## 🔄 Vergleichsprozess

Alle Vergleiche werden **intern auf JSON-Basis** durchgeführt:

```
YAML-Datei 1  ──┐
                ├──> [Parser] ──> OpenAPI-Modell ──┐
JSON-Datei 1  ──┘                                   │
                                                    ├──> [Vergleich] ──> Ergebnis
YAML-Datei 2  ──┐                                   │
                ├──> [Parser] ──> OpenAPI-Modell ──┘
JSON-Datei 2  ──┘
```

**Vorteil:** Unabhängig vom Eingabeformat werden Dateien konsistent verglichen.

## 💡 Verwendungsbeispiele

### 1. Dateien mit unterschiedlichen Formaten vergleichen

```java
OasParser parser = new OasParser(new FormatDetector(), new FormatConverter());

// YAML und JSON mischen
OpenAPI yamlSpec = parser.parseFromFile("api-v1.yaml");
OpenAPI jsonSpec = parser.parseFromFile("api-v2.json");

ComparisonResult result = comparisonService.compareSpecifications(yamlSpec, jsonSpec);
```

### 2. Format programmatisch erkennen

```java
FormatDetector detector = new FormatDetector();

SpecificationFormat format1 = detector.detectFromFilePath("api.yaml");  // YAML
SpecificationFormat format2 = detector.detectFromFilePath("api.json");  // JSON
SpecificationFormat format3 = detector.detectFromContent("{\"openapi\": \"3.0.0\"}");  // JSON
```

### 3. Format konvertieren

```java
FormatConverter converter = new FormatConverter();

// YAML zu JSON
String yamlContent = "openapi: 3.0.0\ninfo:\n  title: My API";
String jsonContent = converter.yamlToJson(yamlContent);
// Ergebnis: {"openapi":"3.0.0","info":{"title":"My API"}}

// JSON zu YAML
String json = "{\"openapi\":\"3.0.0\"}";
String yaml = converter.jsonToYaml(json);
// Ergebnis: openapi: "3.0.0"
```

### 4. Web-Interface mit gemischten Formaten

```bash
# YAML-Datei mit JSON-Datei vergleichen
curl -X POST "http://localhost:8080/api/oas/compare" \
  -d "oldSpecPath=/path/to/api-v1.yaml" \
  -d "newSpecPath=/path/to/api-v2.json"
```

## 🧪 Testing

Umfassende Tests für alle Format-Funktionen:

### FormatDetectorTest
- ✅ YAML-Erkennung (.yaml, .yml)
- ✅ JSON-Erkennung (.json)
- ✅ Case-insensitive Erkennung
- ✅ Content-basierte Erkennung
- ✅ Edge Cases (leer, null)

### FormatConverterTest
- ✅ YAML zu JSON Konvertierung
- ✅ JSON zu YAML Konvertierung
- ✅ Normalisierung zu JSON
- ✅ JsonNode-Parsing
- ✅ Komplexe Strukturen
- ✅ Fehlerbehandlung (ungültige Syntax)

### OasParserFormatIntegrationTest
- ✅ YAML-Datei parsen
- ✅ JSON-Datei parsen
- ✅ YAML-String parsen
- ✅ JSON-String parsen
- ✅ Format-Erkennung
- ✅ Integration mit Spring

## 📦 Abhängigkeiten

Die Format-Unterstützung benötigt folgende Dependencies (in `oas-diff-core/pom.xml`):

```xml
<!-- Jackson für YAML/JSON-Konvertierung -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-yaml</artifactId>
</dependency>
```

## 🎯 Best Practices

### 1. Dateiformat konsistent verwenden
Wähle ein Format (YAML oder JSON) für deine API-Spezifikationen und bleibe dabei. Die Anwendung kann zwar mischen, aber Konsistenz erleichtert die Wartung.

### 2. Dateiendungen korrekt setzen
- Verwende `.yaml` oder `.yml` für YAML-Dateien
- Verwende `.json` für JSON-Dateien
- Dies ermöglicht schnelle Format-Erkennung

### 3. Validierung vor Konvertierung
Stelle sicher, dass deine OAS-Dateien valide sind, bevor du sie konvertierst oder vergleichst.

### 4. UTF-8 Encoding
Speichere alle Dateien mit UTF-8 Encoding, um Probleme mit Sonderzeichen zu vermeiden.

## 🔧 Erweiterbarkeit

Das Design ist offen für weitere Formate:

### Neues Format hinzufügen (z.B. XML)

1. **Enum erweitern:**
```java
public enum SpecificationFormat {
    YAML, JSON, XML, UNKNOWN
}
```

2. **FormatDetector erweitern:**
```java
if (lowerCasePath.endsWith(".xml")) {
    return SpecificationFormat.XML;
}
```

3. **FormatConverter erweitern:**
```java
public String xmlToJson(String xmlContent) {
    // XML zu JSON Konvertierung
}
```

## 🐛 Fehlerbehandlung

Die Implementierung behandelt verschiedene Fehlerszenarien:

- **Ungültige YAML/JSON Syntax**: `OasParseException` mit Details
- **Unbekanntes Format**: Warnung im Log, `UNKNOWN` zurückgegeben
- **Datei nicht gefunden**: `OasParseException` mit Dateipfad
- **Leerer Content**: `OasParseException`

## 📊 Performance

- **Format-Erkennung**: O(1) - sehr schnell
- **YAML→JSON**: Abhängig von Dateigröße, optimiert durch Jackson
- **Vergleich**: Format hat keinen Einfluss auf Performance (beide werden zu OpenAPI-Modell)

## 📝 Logging

Die Komponenten loggen wichtige Schritte:

```
DEBUG - YAML-Format erkannt für: api-v1.yaml
DEBUG - JSON-Format erkannt für: api-v2.json
DEBUG - Konvertiere YAML zu JSON
INFO  - OAS-Datei erfolgreich geparst: api.yaml (Version: 1.0.0, Format: YAML)
```

## 🎓 Zusammenfassung

Die Format-Unterstützung ist:
- ✅ **Modular**: Klare Trennung der Verantwortlichkeiten
- ✅ **Erweiterbar**: Neue Formate einfach hinzufügbar
- ✅ **Getestet**: Umfassende Unit- und Integrationstests
- ✅ **Dokumentiert**: Klare JavaDoc und Beispiele
- ✅ **Wartbar**: Lesbare, gut strukturierte Code-Basis

