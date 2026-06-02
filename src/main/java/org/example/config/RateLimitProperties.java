package org.example.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Налаштування rate limiting з application.yml (секція app.rate-limit).
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    // ліміт на конкретний endpoint (userHash + path)
    private int perEndpointPerMinute = 60;

    // ліміт на користувача в цілому (всі його endpoint-и)
    private int perUserPerMinute = 1000;

    private boolean enabled = true;

}