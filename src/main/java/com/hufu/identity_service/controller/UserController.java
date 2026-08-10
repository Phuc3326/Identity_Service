package com.hufu.identity_service.controller;

import com.hufu.identity_service.dto.request.UserCreationRequest;
import com.hufu.identity_service.dto.request.UserUpdateRequest;
import com.hufu.identity_service.dto.response.ApiResponse;
import com.hufu.identity_service.dto.response.UserResponse;
import com.hufu.identity_service.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
// import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.*;

@Tag(
        name ="User Management",
        description = "Quản lí thông tin người dùng"
)
@Slf4j
@RestController
@RequestMapping("/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserController {
    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder().result(userService.createUser(request)).build();
    }

    @Operation(
            summary = "Lấy thông tin người dùng theo ID",
            description = "Yêu cầu token role ADMIN, trả về thông tin chi tiết người dùng"
    )
    @ApiResponses(
            value={
                    @io.swagger.v3.oas.annotations.responses.ApiResponse (
                            responseCode = "200",
                            description = "Truy vấn người dùng thành công"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Không có quyền (không nhập hoặc sai token)",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema (
                                            implementation = ApiResponse.class
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "ID không tồn tại",
                            content = @Content (
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ApiResponse.class
                                    )
                            )
                    )
            }
    )
    @GetMapping("/{id}")
    ApiResponse<UserResponse> getUser(
            @Parameter(
                    example = "1dcc7612-a72d-4e76-a1b8-b2ae8bffea87",
                    description = "ID người dùng (độc nhất)"
            )
            @PathVariable String id
    ) {
        return ApiResponse.<UserResponse>builder().result(userService.getUser(id)).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    ApiResponse<List<UserResponse>> getUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assert authentication != null;
        log.info(authentication.getName());
        authentication
                .getAuthorities()
                .forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));

        return ApiResponse.<List<UserResponse>>builder().result(userService.getUsers()).build();
    }

    @GetMapping("my-info")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder().result(userService.getMyInfo()).build();
    }

    @PutMapping("/{id}")
    ApiResponse<UserResponse> updateUser(
            @PathVariable String id, @RequestBody @Valid UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ApiResponse.<String>builder().result("User has been deleted").build();
    }
}
