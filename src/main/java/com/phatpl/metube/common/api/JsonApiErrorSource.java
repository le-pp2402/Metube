package com.phatpl.metube.common.api;

public record JsonApiErrorSource(
    String pointer,
    String parameter,
    String header) {
  public static JsonApiErrorSource header(String header) {
    return new JsonApiErrorSource(null, null, header);
  }

  public static JsonApiErrorSource pointer(String pointer) {
    return new JsonApiErrorSource(pointer, null, null);
  }

  public static JsonApiErrorSource parameter(String parameter) {
    return new JsonApiErrorSource(null, parameter, null);
  }
}
