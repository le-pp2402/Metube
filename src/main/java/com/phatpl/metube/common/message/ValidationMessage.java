package com.phatpl.metube.common.message;

public class ValidationMessage {
  public static String notNull(String field) {
    return String.format("%s must not be null", field);
  }

  public static String notBlank(String field) {
    return String.format("%s must not be blank", field);
  }
}
