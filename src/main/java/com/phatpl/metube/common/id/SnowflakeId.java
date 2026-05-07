package com.phatpl.metube.common.id;

import java.nio.ByteBuffer;
import java.util.Base64;

public record SnowflakeId(Long value) {
  public SnowflakeId {
    if (value == null || value <= 0) {
      throw new IllegalArgumentException("SnowflakeId value must be a positive long.");
    }
  }

  public String asBase64() {
    byte[] bytes = ByteBuffer.allocate(Long.BYTES).putLong(value).array();
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public static SnowflakeId fromBase64(String encoded) {
    if (encoded == null || encoded.isBlank()) {
      throw new IllegalArgumentException("Encoded id is required");
    }

    byte[] bytes = Base64.getUrlDecoder().decode(encoded);

    if (bytes.length != Long.BYTES) {
      throw new IllegalArgumentException("Encoded id is invalid");
    }

    long value = ByteBuffer.wrap(bytes).getLong();

    return new SnowflakeId(value);
  }
}
