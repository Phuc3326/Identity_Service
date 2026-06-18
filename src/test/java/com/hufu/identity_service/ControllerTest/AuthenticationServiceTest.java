package com.hufu.identity_service.ControllerTest;

import com.hufu.identity_service.dto.request.AuthenticationRequest;
import com.hufu.identity_service.dto.response.AuthenticationResponse;
import com.hufu.identity_service.entity.User;
import com.hufu.identity_service.exception.AppException;
import com.hufu.identity_service.exception.ErrorCode;
import com.hufu.identity_service.repository.UserRepository;
import com.hufu.identity_service.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    @InjectMocks
    AuthenticationService authenticationService;

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    AuthenticationRequest request;
    User user;

    @BeforeEach
    void setUp() {
        request = AuthenticationRequest.builder()
                .username("JohnHolly")
                .password("12345678")
                .build();
        user = User.builder()
                .username("JohnHolly")
                .password("hashed_password")
                .build();

        ReflectionTestUtils.setField(authenticationService, "SIGNER_KEY",
                "22d14ba61b98c7499371c1838b0c896e5afa40b676ec5e8e4dad58938ed06283");
        ReflectionTestUtils.setField(authenticationService, "EXPIRATION_TIME", 20);
    }
    @Test
    void authenticate_validRequest_success() throws JOSEException {
        Mockito.when(userRepository.findByUsername(request.getUsername()))
                .thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .thenReturn(true);

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertFalse(response.getToken().isEmpty());
    }

    @Test
    void authenticate_wrongPassword_fail() {
        Mockito.when(userRepository.findByUsername(request.getUsername()))
                .thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .thenReturn(false);

        AppException exception = assertThrows(AppException.class, () ->
                authenticationService.authenticate(request));

        assertEquals(ErrorCode.UNAUTHENTICATED, exception.getErrorCode());
    }
}
