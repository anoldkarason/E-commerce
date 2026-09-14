package com.example.ecommerce.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "azampay")
@Data
public class AzamPayConfig {
    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private String appName;
    private String callbackUrl;
    private String authUrl;
    private String tokenUrl;
}