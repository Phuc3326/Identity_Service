package com.hufu.identity_service.exception;

import ch.qos.logback.core.model.INamedModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(1001, "Uncategorized Exception"),
    INVALID_KEY(1002, "INVALID MESSAGE KEY"),
    USER_NOT_EXISTED(1003, "USER NOT FOUND"),
    USER_EXISTED(1004, "USER ALREADY EXISTED"),
    USERNAME_INVALID(1005, "USERNAME MUST BE AT LEAST 4 CHARACTERS" ),
    PASSWORD_INVALID(1006, "PASSWORD MUST BE AT LEAST 8 CHARACTERS"),
    UNAUTHENTICATED(1007, "UNAUTHENTICATED"),
    INVALID_TOKEN_FORMAT(1008, "INVALID TOKEN FORMAT"),
    TOKEN_SIGNING_FAILED(1009, "TOKEN ENCRYPTION OR SIGNING FAILED" )
    ;

    int code;
    String message;
}