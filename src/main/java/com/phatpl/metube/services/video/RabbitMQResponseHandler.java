package com.phatpl.metube.services.video;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public class RabbitMQResponseHandler {
    private ObjectMapper objectMapper = new ObjectMapper();
    private String jsonString;

    public RabbitMQResponseHandler(String jsonString) {
        this.jsonString = jsonString;
    }

    public Root getRoot() throws JsonProcessingException {
        return objectMapper.readValue(jsonString, Root.class);
    }

    public String getKey() throws JsonProcessingException {
        return getRoot().getKey();
    }
}

@Getter
class Root {
    @JsonProperty("EventName")
    public String eventName;

    @JsonProperty("Key")
    public String key;

    @JsonProperty("Records")
    public List<Record> records;
}

@Getter
class Record {
    public String eventVersion;
    public String eventSource;
    public String awsRegion;
    public String eventTime;
    public String eventName;
    public UserIdentity userIdentity;
    public RequestParameters requestParameters;
    public ResponseElements responseElements;
    public S3 s3;
    public Source source;
}

@Getter
class UserIdentity {
    public String principalId;
}

@Getter
class RequestParameters {
    public String principalId;
    public String region;
    public String sourceIPAddress;
}

@Getter
class ResponseElements {
    @JsonProperty("x-amz-id-2")
    public String xAmzId2;

    @JsonProperty("x-amz-request-id")
    public String xAmzRequestId;

    @JsonProperty("x-minio-deployment-id")
    public String xMinioDeploymentId;

    @JsonProperty("x-minio-origin-endpoint")
    public String xMinioOriginEndpoint;
}

@Getter
class S3 {
    public String s3SchemaVersion;
    public String configurationId;
    public Bucket bucket;
    public S3Object object;
}

@Getter
class Bucket {
    public String name;
    public OwnerIdentity ownerIdentity;
    public String arn;
}

@Getter
class OwnerIdentity {
    public String principalId;
}

@Getter
class S3Object {
    public String key;
    public long size;
    public String eTag;
    public String contentType;
    public Map<String, String> userMetadata;
    public String sequencer;
}

@Getter
class Source {
    public String host;
    public String port;
    public String userAgent;
}




