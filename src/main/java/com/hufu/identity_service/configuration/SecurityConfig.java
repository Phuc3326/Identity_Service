package com.hufu.identity_service.configuration;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SecurityConfig {
    CustomJwtDecoder customJwtDecoder;

    @NonFinal
    private final String[] PUBLIC_ENDPOINTS = {
        "/users", "/auth/token", "/auth/introspect", "/auth/logout", "/auth/refresh"
    };

    @Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(
                        oauth2 ->
                                oauth2.jwt(
                                                jwtConfigurer ->
                                                        jwtConfigurer
                                                                .decoder(customJwtDecoder)
                                                                .jwtAuthenticationConverter(
                                                                        jwtAuthenticationConverter()))
                                        .authenticationEntryPoint(
                                                new JwtAuthenticationEntryPoint()))
                .authorizeHttpRequests(
                        request ->
                                request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS)
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated());
        return httpSecurity.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
                new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
