package com.hufu.identity_service.ControllerTest;

import com.hufu.identity_service.controller.UserController;
import com.hufu.identity_service.dto.request.UserCreationRequest;
import com.hufu.identity_service.dto.response.PermissionResponse;
import com.hufu.identity_service.dto.response.RoleResponse;
import com.hufu.identity_service.dto.response.UserResponse;
import com.hufu.identity_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    UserCreationRequest request;
    UserResponse response;

    @BeforeEach
    void init(){
        request = UserCreationRequest.builder()
                .username("JohnHolly")
                .password("12345678")
                .firstName("John")
                .lastName("Holly")
                .dob(LocalDate.of(2000, 1, 1))
                .build();

        PermissionResponse permission = PermissionResponse.builder()
                .name("CREATE")
                .description("Create permission")
                .build();
        Set<PermissionResponse> permissions = new HashSet<>();
        permissions.add(permission);

        RoleResponse role = RoleResponse.builder()
                .name("USER")
                .description("User role")
                .permissions(permissions)
                .build();
        Set<RoleResponse> roles = new HashSet<>();
        roles.add(role);

        response = UserResponse.builder()
                .id("abc")
                .username("JohnHolly")
                .firstName("John")
                .lastName("Holly")
                .dob(LocalDate.of(2000, 1, 1))
                .roles(roles)
                .build();
    }

    @Test
    void createUser_validRequest_Success () throws Exception {
        Mockito.when(userService.createUser(Mockito.any(UserCreationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(response.getId()))
                .andExpect(jsonPath("$.result.username").value(response.getUsername()))
                .andExpect(jsonPath("$.result.firstName").value(response.getFirstName()))
                .andExpect(jsonPath("$.result.lastName").value(response.getLastName()))
                .andExpect(jsonPath("$.result.dob").value(response.getDob().toString()))
                .andExpect(jsonPath("$.result.roles[0].name").value(response
                        .getRoles().iterator().next().getName()))
                .andExpect(jsonPath("$.result.roles[0].permissions[0].name").value(response
                        .getRoles().iterator().next().getPermissions().iterator().next().getName()));
    }

    @Test
    void createUser_tooShortPassword_Failed () throws Exception {
        request.setPassword("123");
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1006));
    }
}
