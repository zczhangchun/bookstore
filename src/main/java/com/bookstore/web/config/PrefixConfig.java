package com.bookstore.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prefix")
@Data
public class PrefixConfig {

    String bookPrefix;
    String cartPrefix;
    String captchaPrefix;
    String PVDaily;
    String turnoverDaily;
    String turnoverTotal;
    String PVTotal;
    String orderQuantityTotal;
    String OrderQuantityDaily;
}
