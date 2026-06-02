package com.hufu.identity_service.mapper;

import com.hufu.identity_service.dto.request.PermissionRequest;
import com.hufu.identity_service.dto.request.RoleRequest;
import com.hufu.identity_service.dto.response.PermissionResponse;
import com.hufu.identity_service.dto.response.RoleResponse;
import com.hufu.identity_service.entity.Permission;
import com.hufu.identity_service.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);
    RoleResponse toRoleResponse(Role role);
}
