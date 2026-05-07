package com.phatpl.metube.common.id;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "metube.snowflake")
public record SnowflakeProperties(long workerId, long dataCenterId) {
}
