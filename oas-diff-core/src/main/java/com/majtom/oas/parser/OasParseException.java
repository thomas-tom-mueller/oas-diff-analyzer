package com.majtom.oas.parser;

/**
 * Exception für Fehler beim Parsen von OAS-Dateien.
 */
public class OasParseException extends Exception {

    public OasParseException(String message) {
        super(message);
    }

    public OasParseException(String message, Throwable cause) {
        super(message, cause);
    }
}

