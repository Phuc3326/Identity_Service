package com.hufu.identity_service.service;

import com.hufu.identity_service.dto.request.UserCreationRequest;
import com.hufu.identity_service.dto.request.UserUpdateRequest;
import com.hufu.identity_service.dto.response.UserResponse;
import com.hufu.identity_service.entity.User;
import com.hufu.identity_service.enums.Role;
import com.hufu.identity_service.exception.AppException;
import com.hufu.identity_service.exception.ErrorCode;
import com.hufu.identity_service.mapper.UserMapper;
import com.hufu.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);

        if (userRepository.existsByUsername(user.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Set<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
        user.setRoles(roles);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    public List<UserResponse> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        User updatedUser = userMapper.updateUser(user, request);
        return userMapper.toUserResponse(userRepository.save(updatedUser));
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}
