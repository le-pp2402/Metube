package com.phatpl.metube.common.exception;

import java.util.List;

public class SchemaValidationException extends RuntimeException {
    private final List<String> errors;

    public SchemaValidationException(List<String> errors) {
        super("Schema validation failed: " + String.join(", ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}