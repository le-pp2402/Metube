package com.phatpl.metube.common.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;
import com.phatpl.metube.common.exception.SchemaValidationException;

@Service
public class SchemaValidatorService {
    private final ObjectMapper objectMapper;
    private final SchemaRegistry schemaRegistry;

    public SchemaValidatorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.schemaRegistry = SchemaRegistry.withDialect(
                Dialects.getDraft202012(),
                builder -> builder
                        .nodeReader(nodeReader -> nodeReader.locationAware())
                        .schemaCacheEnabled(true));
    }

    public void validate(Object payload, String schemaPath) {
        String inputData;
        try {
            inputData = objectMapper.writeValueAsString(payload);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }
        SchemaLocation location = SchemaLocation.of(schemaPath);
        Schema schema = schemaRegistry.getSchema(location);

        List<Error> errors = schema.validate(inputData, InputFormat.JSON, executionContext -> {
            executionContext.executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true));
        });

        if (!errors.isEmpty()) {
            List<String> errorMessages = errors.stream()
                    .map(error -> error != null ? error.getMessage() : null)
                    .collect(Collectors.toList());

            throw new SchemaValidationException(errorMessages);
        }
    }
}
