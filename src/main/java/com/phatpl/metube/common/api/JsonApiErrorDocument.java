package com.phatpl.metube.common.api;

import java.util.List;

public record JsonApiErrorDocument(
    JsonApiObject jsonapi,
    List<JsonApiError> errors
  ) {
  public static JsonApiErrorDocument of(JsonApiError error) {
    return new JsonApiErrorDocument(
        new JsonApiObject("1.1"),
        List.of(error)
      );
  }
}
