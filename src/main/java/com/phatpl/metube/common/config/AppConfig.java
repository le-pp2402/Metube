package com.phatpl.metube.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import com.phatpl.metube.common.id.SnowflakeProperties;

@Configuration
@EnableConfigurationProperties(SnowflakeProperties.class)
public class AppConfig {

}
