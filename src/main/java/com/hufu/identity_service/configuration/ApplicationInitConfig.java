package com.hufu.identity_service.configuration;

import com.hufu.identity_service.entity.Role;
import com.hufu.identity_service.entity.User;
import com.hufu.identity_service.enums.RoleEnum;
import com.hufu.identity_service.exception.AppException;
import com.hufu.identity_service.repository.RoleRepository;
import com.hufu.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

import static com.hufu.identity_service.exception.ErrorCode.ROLE_NOT_EXISTED;

@Slf4j
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Profile("!test")
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository,
                                        RoleRepository roleRepository) {
        return args -> {
            if(userRepository.findByUsername("admin").isEmpty()) {

                if (!roleRepository.existsById("ADMIN")) {
                    roleRepository.save(Role.builder()
                            .name(RoleEnum.ADMIN.name())
                            .description("Role Admin").build());
                }
                Role role = roleRepository.findById(RoleEnum.ADMIN.name())
                        .orElseThrow(() -> new AppException(ROLE_NOT_EXISTED));

                Set<Role> roles = new HashSet<>();
                roles.add(role);
                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .roles(roles)
                        .build();
                userRepository.save(user);

                log.warn("admin user has been created with default password: admin, please change it!");
            }
        };
    }
}
