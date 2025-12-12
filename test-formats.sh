#!/bin/bash

# Test-Script für YAML und JSON Format-Unterstützung
# Dieses Script demonstriert die Format-Unterstützung des OAS Diff Analyzers

echo "======================================"
echo "OAS Diff Analyzer - Format Test"
echo "======================================"
echo ""

# Projektstamm
PROJECT_ROOT="/Users/tom/repos/java/oas-diff-analyzer"
EXAMPLES_DIR="$PROJECT_ROOT/oas-diff-examples/src/main/resources/examples"

# Prüfe, ob der Server läuft
echo "📡 Prüfe Server-Verfügbarkeit..."
if ! curl -s http://localhost:8080 > /dev/null 2>&1; then
    echo "❌ Server läuft nicht auf http://localhost:8080"
    echo "   Bitte starte den Server mit: cd oas-diff-web && mvn spring-boot:run"
    exit 1
fi
echo "✅ Server ist erreichbar"
echo ""

# Test 1: YAML mit YAML vergleichen
echo "🔍 Test 1: YAML mit YAML vergleichen"
echo "   Alte Datei: todo-api-v1.yaml"
echo "   Neue Datei: todo-api-v2.yaml"
curl -s -X POST "http://localhost:8080/api/oas/compare" \
  -d "oldSpecPath=$EXAMPLES_DIR/todo-api-v1.yaml" \
  -d "newSpecPath=$EXAMPLES_DIR/todo-api-v2.yaml" \
  | head -n 20
echo ""
echo ""

# Test 2: JSON mit JSON vergleichen
echo "🔍 Test 2: JSON mit JSON vergleichen"
echo "   Alte Datei: oas3-compare-old.json"
echo "   Neue Datei: oas3-compare-new.json"
curl -s -X POST "http://localhost:8080/api/oas/compare" \
  -d "oldSpecPath=$EXAMPLES_DIR/oas3-compare-old.json" \
  -d "newSpecPath=$EXAMPLES_DIR/oas3-compare-new.json" \
  | head -n 20
echo ""
echo ""

# Test 3: YAML mit JSON vergleichen (mixed)
echo "🔍 Test 3: YAML mit JSON vergleichen (Mixed)"
echo "   Alte Datei: todo-api-v1.yaml (YAML)"
echo "   Neue Datei: oas3-compare-new.json (JSON)"
echo "   ℹ️  Dies demonstriert die Format-Unabhängigkeit"
curl -s -X POST "http://localhost:8080/api/oas/compare" \
  -d "oldSpecPath=$EXAMPLES_DIR/todo-api-v1.yaml" \
  -d "newSpecPath=$EXAMPLES_DIR/oas3-compare-new.json" \
  | head -n 20
echo ""
echo ""

echo "======================================"
echo "✅ Alle Format-Tests abgeschlossen!"
echo "======================================"
echo ""
echo "💡 Hinweise:"
echo "   - Die Anwendung erkennt Formate automatisch anhand der Dateiendung"
echo "   - YAML (.yaml, .yml) und JSON (.json) werden unterstützt"
echo "   - Formate können beliebig gemischt werden"
echo "   - Alle Vergleiche werden intern auf JSON-Basis durchgeführt"
echo ""

