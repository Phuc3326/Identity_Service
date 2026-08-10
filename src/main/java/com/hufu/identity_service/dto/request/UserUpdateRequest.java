package com.hufu.identity_service.dto.request;

import com.hufu.identity_service.validator.DobConstraint;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @Size(min = 8, message = "PASSWORD_INVALID")
    String password;

    String firstName;
    String lastName;

    @Schema(
            type="string",
            pattern="yyyy-MM-dd",
            description = "Ngày sinh",
            example = "2006-03-03"
    )
    @DobConstraint(min = 18, message = "INVALID_DOB")
    LocalDate dob;

    Set<String> roles;
}
