package com.hufu.identity_service.exception;

import ch.qos.logback.core.model.INamedModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(1001, "Uncategorized Exception", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1002, "INVALID MESSAGE KEY", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1003, "USER NOT FOUND", HttpStatus.NOT_FOUND),
    USER_EXISTED(1004, "USER ALREADY EXISTED", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1005, "USERNAME MUST BE AT LEAST {min} CHARACTERS", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1006, "PASSWORD MUST BE AT LEAST {min} CHARACTERS", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN_FORMAT(1008, "INVALID TOKEN FORMAT", HttpStatus.BAD_REQUEST),
    TOKEN_SIGNING_FAILED(1009, "TOKEN ENCRYPTION OR SIGNING FAILED", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1007, "UNAUTHENTICATED", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1008, "YOU DO NOT HAVE PERMISSION", HttpStatus.FORBIDDEN),
    ROLE_NOT_EXISTED(1010, "ROLE NOT FOUND", HttpStatus.NOT_FOUND),
    INVALID_DOB(1011, "YOU HAVE TO BE AT LEAST {min} YEARS OLD", HttpStatus.BAD_REQUEST)
    ;

    int code;
    String message;
    HttpStatusCode httpStatusCode;
}