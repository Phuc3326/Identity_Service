package com.hufu.identity_service.controller;

import com.hufu.identity_service.dto.request.AuthenticationRequest;
import com.hufu.identity_service.dto.request.IntrospectRequest;
import com.hufu.identity_service.dto.request.LogoutRequest;
import com.hufu.identity_service.dto.request.RefreshRequest;
import com.hufu.identity_service.dto.response.ApiResponse;
import com.hufu.identity_service.dto.response.AuthenticationResponse;
import com.hufu.identity_service.dto.response.IntrospectResponse;
import com.hufu.identity_service.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import java.text.ParseException;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name ="Authentication",
        description = "Quản lí thông tin xác thực"
)
@RestController
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request)
            throws JOSEException {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.authenticate(request))
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws JOSEException, ParseException {
        return ApiResponse.<IntrospectResponse>builder()
                .result(authenticationService.introspect(request))
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<String> logout(@RequestBody LogoutRequest request)
            throws JOSEException, ParseException {
        authenticationService.logout(request);
        return ApiResponse.<String>builder().result("Logout successfully!").build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request)
            throws JOSEException, ParseException {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.refreshToken(request))
                .build();
    }
}
