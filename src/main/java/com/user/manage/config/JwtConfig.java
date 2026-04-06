package com.user.manage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret;
    private long accessTokenExpiryMs;
    private long refreshTokenExpiryMs;
}
