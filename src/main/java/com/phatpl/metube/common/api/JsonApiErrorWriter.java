package com.phatpl.metube.common.api;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonApiErrorWriter {
  public static final String JSON_API_MEDIA_TYPE = "application/vnd.api+json";
  private final ObjectMapper objectMapper;

  public JsonApiErrorWriter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  // public void write()
}
