package com.hufu.identity_service.service;

import com.hufu.identity_service.dto.request.UserCreationRequest;
import com.hufu.identity_service.dto.request.UserUpdateRequest;
import com.hufu.identity_service.dto.response.UserResponse;
import com.hufu.identity_service.entity.Role;
import com.hufu.identity_service.entity.User;
import com.hufu.identity_service.enums.RoleEnum;
import com.hufu.identity_service.exception.AppException;
import com.hufu.identity_service.exception.ErrorCode;
import com.hufu.identity_service.mapper.UserMapper;
import com.hufu.identity_service.repository.RoleRepository;
import com.hufu.identity_service.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role role =
                roleRepository
                        .findById(RoleEnum.USER.name())
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        roles.add(role);
        user.setRoles(roles);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(user);
    }

    @Cacheable(value = "users", key = "#id")
    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUser(String id) {
        log.info("Querying SQL...");
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    public UserResponse getMyInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        User user =
                userRepository
                        .findByUsername(authentication.getName())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    @CachePut(value = "users", key = "#id")
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        User updatedUser = userMapper.updateUser(user, request);

        updatedUser.setPassword(passwordEncoder.encode(request.getPassword()));
        List<Role> roles = roleRepository.findAllById(request.getRoles());
        updatedUser.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(updatedUser));
    }

    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}
