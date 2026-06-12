package com.hufu.identity_service.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;

@Configuration
public class UtilConfig {
    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    NimbusJwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");
//        Turn off the Clock Skew mechanism by setting the grace period to ZERO
//        OAuth2TokenValidator<Jwt> jwtValidator = new JwtTimestampValidator(Duration.ZERO);
//        nimbusJwtDecoder.setJwtValidator(jwtValidator);
        return NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm((MacAlgorithm.HS512))
                .build();
    }
}
