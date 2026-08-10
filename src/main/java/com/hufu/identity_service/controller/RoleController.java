package com.hufu.identity_service.controller;

import com.hufu.identity_service.dto.request.RoleRequest;
import com.hufu.identity_service.dto.response.ApiResponse;
import com.hufu.identity_service.dto.response.RoleResponse;
import com.hufu.identity_service.service.RoleService;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(
        name ="Role Management",
        description = "Quản lí thông tin role"
)
@Slf4j
@RestController
@RequestMapping("/role")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RoleController {
    RoleService roleService;

    @PostMapping
    ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) {
        return ApiResponse.<RoleResponse>builder().result(roleService.create(request)).build();
    }

    @GetMapping
    ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.<List<RoleResponse>>builder().result(roleService.getAll()).build();
    }

    @DeleteMapping("/{role}")
    ApiResponse<String> delete(@PathVariable String role) {
        roleService.delete(role);
        return ApiResponse.<String>builder().result("Role has been deleted").build();
    }
}
