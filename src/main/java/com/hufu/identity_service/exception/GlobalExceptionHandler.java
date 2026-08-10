package com.hufu.identity_service.exception;

import com.hufu.identity_service.dto.response.ApiResponse;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.ConstraintViolation;
import java.text.ParseException;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<?>> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse<?> apiResponse =
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    ResponseEntity<ApiResponse<?>> handlingNoResourceFoundException(NoResourceFoundException exception) {
        ErrorCode errorCode = ErrorCode.USER_NOT_EXISTED;
        ApiResponse<?> apiResponse =
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<?>> handlingMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        String errorKey = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        String message = errorCode.getMessage();
        try {
            errorCode = ErrorCode.valueOf(errorKey);

            ConstraintViolation<?> constraintViolation =
                    exception
                            .getBindingResult()
                            .getAllErrors()
                            .getFirst()
                            .unwrap(ConstraintViolation.class);
            Map<String, Object> attributes =
                    constraintViolation.getConstraintDescriptor().getAttributes();

            message = mapAttributes(errorCode.getMessage(), attributes);
        } catch (IllegalArgumentException ignored) {
        }

        ApiResponse<?> apiResponse =
                ApiResponse.builder().code(errorCode.getCode()).message(message).build();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    String mapAttributes(String message, Map<String, Object> attributes) {
        String specific = attributes.get(MIN_ATTRIBUTE).toString();
        return message.replace("{" + MIN_ATTRIBUTE + "}", specific);
    }

    @ExceptionHandler(value = JOSEException.class)
    ResponseEntity<ApiResponse<?>> handlingJOSEException(JOSEException exception) {
        ErrorCode errorCode = ErrorCode.TOKEN_SIGNING_FAILED;
        ApiResponse<?> apiResponse =
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = ParseException.class)
    ResponseEntity<ApiResponse<?>> handlingParseException(ParseException exception) {
        ErrorCode errorCode = ErrorCode.INVALID_TOKEN_FORMAT;
        ApiResponse<?> apiResponse =
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse<?>> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        ApiResponse<?> apiResponse =
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<?>> handlingException(Exception exception) {
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        ApiResponse<?> apiResponse =
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }
}
