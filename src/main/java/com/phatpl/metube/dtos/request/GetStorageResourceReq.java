package com.phatpl.metube.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetStorageResourceReq {
    @JsonProperty("resource_path")
    public String resourcePath;
}
