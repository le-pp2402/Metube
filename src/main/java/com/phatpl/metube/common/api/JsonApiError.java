package com.phatpl.metube.common.api;

import java.util.Map;

public record JsonApiError(
    String id,
    String status,
    String code,
    String title,
    String detail,
    JsonApiErrorSource source,
    Map<String, Object> meta) {

}
