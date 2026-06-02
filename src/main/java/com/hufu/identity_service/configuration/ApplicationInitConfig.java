package com.hufu.identity_service.configuration;

import com.hufu.identity_service.entity.Role;
import com.hufu.identity_service.entity.User;
import com.hufu.identity_service.enums.RoleEnum;
import com.hufu.identity_service.repository.RoleRepository;
import com.hufu.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository,
                                        RoleRepository roleRepository) {
        return args -> {
            if(userRepository.findByUsername("admin").isEmpty()) {
                Set<Role> roles = new HashSet<>();
                Role role = roleRepository.findById(RoleEnum.ADMIN.name()).orElseThrow();
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
