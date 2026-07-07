package com.hufu.identity_service.ServiceTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hufu.identity_service.dto.request.UserCreationRequest;
import com.hufu.identity_service.dto.response.UserResponse;
import com.hufu.identity_service.entity.Permission;
import com.hufu.identity_service.entity.Role;
import com.hufu.identity_service.entity.User;
import com.hufu.identity_service.exception.AppException;
import com.hufu.identity_service.exception.ErrorCode;
import com.hufu.identity_service.repository.RoleRepository;
import com.hufu.identity_service.repository.UserRepository;
import com.hufu.identity_service.service.UserService;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource("/test.properties")
class UserServiceTest {
    @Autowired UserService userService;

    @Autowired PasswordEncoder passwordEncoder;

    @MockitoBean UserRepository userRepository;
    @MockitoBean RoleRepository roleRepository;

    UserCreationRequest request;
    Role userRole;
    User savedUser;

    @BeforeEach
    void initData() {
        request =
                UserCreationRequest.builder()
                        .username("JohnHolly")
                        .password("12345678")
                        .firstName("John")
                        .lastName("Holly")
                        .dob(LocalDate.of(2000, Month.JANUARY, 1))
                        .build();

        Permission permission =
                Permission.builder()
                        .name("PERMISSION_NAME")
                        .description("Permission description")
                        .build();
        Set<Permission> permissions = Set.of(permission);
        userRole =
                Role.builder()
                        .name("USER")
                        .description("User default role")
                        .permissions(permissions)
                        .build();

        savedUser =
                User.builder()
                        .id("123456")
                        .username("JohnHolly")
                        .password(passwordEncoder.encode("12345678"))
                        .firstName("John")
                        .lastName("Holly")
                        .dob(LocalDate.of(2000, Month.JANUARY, 1))
                        .roles(Set.of(userRole))
                        .build();
    }

    @Test
    void createUser_validRequest_Success() {
        Mockito.when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        Mockito.when(roleRepository.findById("USER")).thenReturn(Optional.of(userRole));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertEquals(response.getUsername(), request.getUsername());
        assertEquals(response.getFirstName(), request.getFirstName());
        assertEquals(response.getLastName(), request.getLastName());
        assertEquals(response.getDob(), request.getDob());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }

    @Test
    void createUser_userExisted_Failure() {
        Mockito.when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        AppException exception =
                assertThrows(AppException.class, () -> userService.createUser(request));

        assertEquals(ErrorCode.USER_EXISTED, exception.getErrorCode());
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }
}
