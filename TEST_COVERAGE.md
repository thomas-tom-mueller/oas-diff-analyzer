# Test OAS Files - Breaking Change Coverage

## Übersicht

Die Todo-API Test-Dateien (v1 und v2) wurden so erweitert, dass beim Vergleich **alle 20 implementierten Breaking Change Rules** mindestens einmal ausgelöst werden.

## Abgedeckte Breaking Change Rules

### 1. ✅ ENDPOINT_REMOVED
**Location**: `/categories` Endpoint
- V1: Vorhanden (GET, POST)
- V2: Komplett entfernt
- **Severity**: CRITICAL

### 2. ✅ ENDPOINT_ADDED
**Location**: `/health` Endpoint
- V1: Nicht vorhanden
- V2: Neu hinzugefügt (GET)
- **Severity**: INFO (non-breaking)

### 3. ✅ METHOD_REMOVED
**Location**: `/users/{userId}` DELETE
- V1: DELETE Methode vorhanden
- V2: DELETE Methode entfernt
- **Severity**: CRITICAL

### 4. ✅ METHOD_ADDED
**Location**: `/users/{userId}/todos/{todoId}` PATCH
- V1: PATCH Methode nicht vorhanden
- V2: PATCH Methode hinzugefügt
- **Severity**: INFO (non-breaking)

### 5. ✅ PARAMETER_REMOVED
**Location**: `/users/{userId}/todos/{todoId}` GET - Parameter `includeDetails`
- V1: Parameter `includeDetails` vorhanden (optional)
- V2: Parameter wurde entfernt
- **Severity**: MINOR (war optional)

### 6. ✅ PARAMETER_ADDED
**Location**: `/users/{userId}/todos/{todoId}` GET - Parameter `includeHistory`
- V1: Parameter nicht vorhanden
- V2: Neuer optionaler Parameter hinzugefügt
- **Severity**: INFO (non-breaking)

### 7. ✅ PARAMETER_REQUIRED_ADDED
**Location**: `/users` GET - Parameter `includeInactive`
- V1: Parameter nicht vorhanden
- V2: Neuer Required-Parameter
- **Severity**: CRITICAL

**Location**: `/users/{userId}/todos/{todoId}` GET - Parameter `format`
- V1: Optional
- V2: Required (optional → required)
- **Severity**: MAJOR

### 8. ✅ PARAMETER_TYPE_CHANGED
**Location**: `/users/{userId}/todos/{todoId}` GET - Parameter `format`
- V1: Type `string`
- V2: Type `integer`
- **Severity**: MAJOR

### 9. ✅ RESPONSE_CODE_REMOVED
**Location**: `/users/{userId}/todos/{todoId}` GET - Response `410`
- V1: Response 410 vorhanden
- V2: Response 410 entfernt
- **Severity**: MINOR (nicht Success-Code)

### 10. ✅ RESPONSE_CODE_CHANGED
**Location**: `/users` POST
- V1: Success-Code `200`
- V2: Success-Code `201`
- **Severity**: MAJOR

### 11. ✅ RESPONSE_SCHEMA_CHANGED
**Location**: `/users/{userId}/todos/{todoId}` GET
- V1: Response-Schema `Todo`
- V2: Response-Schema `TodoDetailed`
- **Severity**: MAJOR

### 12. ✅ REQUEST_SCHEMA_CHANGED
**Location**: `/users/{userId}/todos/{todoId}` PUT
- V1: Request-Schema `TodoInput`
- V2: Request-Schema `TodoUpdateInput`
- **Severity**: MAJOR

### 13. ✅ PROPERTY_REMOVED
**Location**: Schema `Todo` - Property `categoryId`
- V1: Property vorhanden
- V2: Property entfernt
- **Severity**: MAJOR

**Location**: Schema `Category` (komplettes Schema)
- V1: Schema vorhanden
- V2: Schema entfernt (weil Endpoint entfernt)
- **Severity**: MAJOR

### 14. ✅ PROPERTY_ADDED
**Location**: Schema `User` - Property `isActive`
- V1: Property nicht vorhanden
- V2: Neue optionale Property
- **Severity**: INFO (non-breaking)

**Location**: Schema `Todo` - Property `dueDate`
- V1: Property nicht vorhanden
- V2: Neue optionale Property
- **Severity**: INFO (non-breaking)

### 15. ✅ PROPERTY_REQUIRED_ADDED
**Location**: Schema `User` - Property `accountType`
- V1: Property nicht vorhanden
- V2: Neues Required-Feld
- **Severity**: CRITICAL

**Location**: Schema `UserInput` - Property `accountType`
- V1: Property nicht vorhanden
- V2: Neues Required-Feld
- **Severity**: CRITICAL

**Location**: Schema `TodoInput` - Property `priority`
- V1: Optional
- V2: Required (optional → required)
- **Severity**: MAJOR

### 16. ✅ PROPERTY_TYPE_CHANGED
**Location**: Schema `Todo` - Property `updatedAt`
- V1: Type `string` (format: date-time)
- V2: Type `integer` (Unix timestamp)
- **Severity**: MAJOR

### 17. ✅ ENUM_VALUE_REMOVED
**Location**: Schema `Todo` - Property `priority` - Enum-Wert `URGENT`
- V1: Enum-Wert `URGENT` vorhanden
- V2: Enum-Wert `URGENT` entfernt
- **Severity**: MAJOR

### 18. ✅ ENUM_VALUE_ADDED
**Location**: Schema `Todo` - Property `status` - Enum-Wert `CANCELLED`
- V1: Enum-Wert nicht vorhanden
- V2: Enum-Wert `CANCELLED` hinzugefügt
- **Severity**: INFO (non-breaking)

### 19. ✅ API_VERSION_CHANGED
**Location**: Info Section - Version
- V1: Version `1.0.0`
- V2: Version `2.0.0`
- **Severity**: CRITICAL (Major-Version-Änderung)

### 20. ✅ BASE_PATH_CHANGED
**Location**: Server URL
- V1: `https://api.example.com/v1`
- V2: `https://api.example.com/v2`
- **Severity**: CRITICAL

## Zusammenfassung

**Total Coverage**: 20/20 Rules (100%)

### Breaking Changes: 14
- ENDPOINT_REMOVED
- METHOD_REMOVED
- PARAMETER_REMOVED (optional)
- PARAMETER_REQUIRED_ADDED
- PARAMETER_TYPE_CHANGED
- RESPONSE_CODE_REMOVED (non-success)
- RESPONSE_CODE_CHANGED
- RESPONSE_SCHEMA_CHANGED
- REQUEST_SCHEMA_CHANGED
- PROPERTY_REMOVED
- PROPERTY_REQUIRED_ADDED
- PROPERTY_TYPE_CHANGED
- ENUM_VALUE_REMOVED
- API_VERSION_CHANGED
- BASE_PATH_CHANGED

### Non-Breaking Changes: 6
- ENDPOINT_ADDED
- METHOD_ADDED
- PARAMETER_ADDED
- PROPERTY_ADDED
- ENUM_VALUE_ADDED

## Dateien

Alle Änderungen sind sowohl in YAML als auch JSON Format verfügbar:

- `todo-api-v1.yaml` - Baseline Version (erweitert)
- `todo-api-v2.yaml` - Version mit allen Breaking Changes
- `todo-api-v1.json` - JSON-Version der Baseline
- `todo-api-v2.json` - JSON-Version mit Breaking Changes

## Test-Ausführung

Um alle Rules zu testen, führen Sie einen Vergleich der beiden Versionen aus:

```bash
# Via Maven
mvn spring-boot:run -pl oas-diff-web

# Dann in der Web-UI:
# - Upload: todo-api-v1.yaml (oder .json)
# - Upload: todo-api-v2.yaml (oder .json)
# - Vergleich starten
```

Der Vergleich sollte alle 20 unterschiedlichen Change-Types erkennen und anzeigen.

