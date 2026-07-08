package com.phatpl.metube.common.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;

@Service
public class YamlSchemaValidatorService {
    private final ObjectMapper objectMapper;
    private final SchemaRegistry schemaRegistry;

    private final Map<String, Schema> schemaCache = new ConcurrentHashMap<>();

    public YamlSchemaValidatorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.schemaRegistry = SchemaRegistry.withDialect(
                Dialects.getDraft202012(),
                builder -> builder.nodeReader(nodeReader -> nodeReader.locationAware()));
    }

}
