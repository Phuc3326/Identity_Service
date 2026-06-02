package com.hufu.identity_service.repository;

import com.hufu.identity_service.entity.Permission;
import com.hufu.identity_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
}
