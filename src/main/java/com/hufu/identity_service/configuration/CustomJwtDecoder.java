package com.hufu.identity_service.configuration;

import com.hufu.identity_service.service.AuthenticationService;
import java.text.ParseException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {
    AuthenticationService authenticationService;
    NimbusJwtDecoder nimbusJwtDecoder;

    @Override
    public Jwt decode(String token) throws JwtException {
        Jwt jwt = nimbusJwtDecoder.decode(token);

        try {
            if (authenticationService.isLoggedOut(jwt.getId()))
                throw new JwtException("You have logged out!");
        } catch (ParseException e) {
            throw new JwtException(e.getMessage());
        }

        return jwt;
    }
}
