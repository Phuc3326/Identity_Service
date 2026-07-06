package com.hufu.identity_service.IntegrationTest;

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
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Testcontainers
public class UserServiceIntegrationTest {
    @Container
    static final MySQLContainer<?> MY_SQL_CONTAINER =
            new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MY_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MY_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MY_SQL_CONTAINER::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.datasource.driverClassName", ()-> "com.mysql.cj.jdbc.Driver");
    }

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;

    UserCreationRequest request;
    Role userRole;
    User savedUser;

    @BeforeEach
    void initData() {
        request = UserCreationRequest.builder()
                .username("JohnHolly")
                .password("12345678")
                .firstName("John")
                .lastName("Holly")
                .dob(LocalDate.of(2000, 1, 1))
                .build();

        Permission permission = Permission.builder()
                .name("PERMISSION_NAME")
                .description("Permission description")
                .build();
        Set<Permission> permissions = Set.of(permission);
        userRole = Role.builder()
                .name("USER")
                .description("User default role")
                .permissions(permissions)
                .build();
        roleRepository.save(userRole);

        savedUser = User.builder()
                .id("123456")
                .username("JohnHolly")
                .password(passwordEncoder.encode("12345678"))
                .firstName("John")
                .lastName("Holly")
                .dob(LocalDate.of(2000, 1, 1))
                .roles(Set.of(userRole))
                .build();
    }

    @Test
    void createUser_validRequest_Success() {
        UserResponse response = userService.createUser(request);

        assertEquals(response.getUsername(), request.getUsername());
        assertEquals(response.getFirstName(), request.getFirstName());
        assertEquals(response.getLastName(), request.getLastName());
        assertEquals(response.getDob(), request.getDob());
        assertTrue(userRepository.existsByUsername(request.getUsername()));

        log.info("Result ID: {}", response.getId());
    }

    @Test
    void createUser_userExisted_Failure() {
        userRepository.save(savedUser);

        AppException exception = assertThrows(AppException.class, () ->
                userService.createUser(request));

        assertEquals(ErrorCode.USER_EXISTED, exception.getErrorCode());
        assertFalse(userRepository.existsByUsername(request.getUsername()));

    }
}