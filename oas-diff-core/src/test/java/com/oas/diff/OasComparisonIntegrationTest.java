package com.oas.diff;

import com.oas.diff.analyzer.OasComparisonService;
import com.oas.diff.model.ComparisonResult;
import com.oas.diff.parser.FormatConverter;
import com.oas.diff.parser.FormatDetector;
import com.oas.diff.parser.OasParser;
import com.oas.diff.rules.EndpointRemovedRule;
import com.oas.diff.rules.MethodRemovedRule;
import com.oas.diff.rules.RequiredParameterAddedRule;
import com.oas.diff.rules.ResponseCodeChangedRule;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-Test für den OAS-Vergleich.
 */
class OasComparisonIntegrationTest {

    private OasComparisonService comparisonService;
    private OasParser parser;

    @BeforeEach
    void setUp() {
        FormatDetector formatDetector = new FormatDetector();
        FormatConverter formatConverter = new FormatConverter();
        parser = new OasParser(formatDetector, formatConverter);
        comparisonService = new OasComparisonService(
                parser,
                Arrays.asList(
                        new EndpointRemovedRule(),
                        new MethodRemovedRule(),
                        new RequiredParameterAddedRule(),
                        new ResponseCodeChangedRule()
                )
        );
    }

    @Test
    void testSimpleComparison() {
        // Erstelle zwei einfache OAS-Spezifikationen
        String v1 = """
                openapi: 3.0.0
                info:
                  title: Test API
                  version: 1.0.0
                paths:
                  /test:
                    get:
                      responses:
                        '200':
                          description: OK
                """;

        String v2 = """
                openapi: 3.0.0
                info:
                  title: Test API
                  version: 2.0.0
                paths:
                  /test:
                    get:
                      responses:
                        '200':
                          description: OK
                """;

        try {
            OpenAPI oldSpec = parser.parseFromString(v1);
            OpenAPI newSpec = parser.parseFromString(v2);

            ComparisonResult result = comparisonService.compareSpecifications(oldSpec, newSpec);

            assertNotNull(result);
            assertEquals("1.0.0", result.getOldVersion());
            assertEquals("2.0.0", result.getNewVersion());

        } catch (Exception e) {
            fail("Test sollte nicht fehlschlagen: " + e.getMessage());
        }
    }

    @Test
    void testEndpointRemoved() {
        String v1 = """
                openapi: 3.0.0
                info:
                  title: Test API
                  version: 1.0.0
                paths:
                  /test:
                    get:
                      responses:
                        '200':
                          description: OK
                  /removed:
                    get:
                      responses:
                        '200':
                          description: OK
                """;

        String v2 = """
                openapi: 3.0.0
                info:
                  title: Test API
                  version: 2.0.0
                paths:
                  /test:
                    get:
                      responses:
                        '200':
                          description: OK
                """;

        try {
            OpenAPI oldSpec = parser.parseFromString(v1);
            OpenAPI newSpec = parser.parseFromString(v2);

            ComparisonResult result = comparisonService.compareSpecifications(oldSpec, newSpec);

            assertTrue(result.hasBreakingChanges(), "Sollte Breaking Changes enthalten");
            assertTrue(result.getBreakingChangesCount() > 0, "Sollte mindestens einen Breaking Change haben");

        } catch (Exception e) {
            fail("Test sollte nicht fehlschlagen: " + e.getMessage());
        }
    }

    @Test
    void testMethodRemoved() {
        String v1 = """
                openapi: 3.0.0
                info:
                  title: Test API
                  version: 1.0.0
                paths:
                  /test:
                    get:
                      responses:
                        '200':
                          description: OK
                    delete:
                      responses:
                        '204':
                          description: Deleted
                """;

        String v2 = """
                openapi: 3.0.0
                info:
                  title: Test API
                  version: 2.0.0
                paths:
                  /test:
                    get:
                      responses:
                        '200':
                          description: OK
                """;

        try {
            OpenAPI oldSpec = parser.parseFromString(v1);
            OpenAPI newSpec = parser.parseFromString(v2);

            ComparisonResult result = comparisonService.compareSpecifications(oldSpec, newSpec);

            assertTrue(result.hasBreakingChanges(), "DELETE-Entfernung sollte Breaking Change sein");

        } catch (Exception e) {
            fail("Test sollte nicht fehlschlagen: " + e.getMessage());
        }
    }
}

