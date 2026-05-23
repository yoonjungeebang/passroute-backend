package passroutebackend.global.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import passroutebackend.global.ApiResponse;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException ex) {
    ErrorCode errorCode = ex.getErrorCode();

    log.warn("CustomException occurred. code={}, message={}", errorCode.getCode(), ex.getMessage());

    return ResponseEntity
      .status(errorCode.getStatus())
      .body(ApiResponse.error(errorCode, ex.getMessage(), null));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    log.warn("Validation failed", ex);

    Map<String, String> errors = extractValidationErrors(ex.getBindingResult());

    return ResponseEntity
      .status(ErrorCode.INVALID_INPUT.getStatus())
      .body(ApiResponse.error(ErrorCode.INVALID_INPUT, errors));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<?>> handleConstraintViolationException(ConstraintViolationException ex) {
    log.warn("Constraint violation", ex);

    Map<String, String> errors = new HashMap<>();
    ex.getConstraintViolations().forEach(violation -> {
      String field = violation.getPropertyPath().toString();
      errors.put(field, violation.getMessage());
    });

    return ResponseEntity
      .status(ErrorCode.INVALID_INPUT.getStatus())
      .body(ApiResponse.error(ErrorCode.INVALID_INPUT, errors));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiResponse<?>> handleNoResourceFoundException(NoResourceFoundException ex) {
    log.warn("NoResourceFoundException", ex);

    return ResponseEntity
      .status(ErrorCode.RESOURCE_NOT_FOUND.getStatus())
      .body(ApiResponse.error(ErrorCode.RESOURCE_NOT_FOUND));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse<?>> handleHttpRequestMethodNotSupportedException(
    HttpRequestMethodNotSupportedException ex
  ) {
    log.warn("HttpRequestMethodNotSupportedException", ex);

    return ResponseEntity
      .status(ErrorCode.METHOD_NOT_ALLOWED.getStatus())
      .body(ApiResponse.error(ErrorCode.METHOD_NOT_ALLOWED));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handleException(Exception ex) {
    log.error("Unhandled exception occurred", ex);

    return ResponseEntity
      .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
      .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
  }

  private Map<String, String> extractValidationErrors(BindingResult bindingResult) {
    Map<String, String> errors = new HashMap<>();

    for (FieldError error : bindingResult.getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }

    return errors;
  }
}