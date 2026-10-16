package com.liu.permission.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** 签名密钥，长度需 >= 32 字节 */
    private String secret;
    /** 访问令牌有效期（分钟） */
    private long accessTokenMinutes = 60;
    /** 刷新令牌有效期（天） */
    private long refreshTokenDays = 7;
}
