package com.bookstore.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "pay")
public class PayConfig {

    private String appID;
    private String mchID;
    private String key;
    private String httpConnectTimeoutMs;
    private String httpReadTimeoutMs;
    private String notifyUrl;

}
