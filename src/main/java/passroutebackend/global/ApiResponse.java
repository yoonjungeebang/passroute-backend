package passroutebackend.global;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import passroutebackend.global.exception.ErrorCode;

@Getter
public class ApiResponse<T> {

  public static final String STATUS_SUCCESS = "success";
  public static final String STATUS_ERROR = "error";

  private final String status;
  private final int httpStatus;
  private final String code;
  private final String message;
  private final T data;

  private ApiResponse(String status, int httpStatus, String code, String message, T data) {
    this.status = status;
    this.httpStatus = httpStatus;
    this.code = code;
    this.message = message;
    this.data = data;
  }

  public static <T> ApiResponse<T> success() {
    return new ApiResponse<>(
      STATUS_SUCCESS,
      HttpStatus.OK.value(),
      "SUCCESS",
      "요청이 성공했습니다.",
      null
    );
  }

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(
      STATUS_SUCCESS,
      HttpStatus.OK.value(),
      "SUCCESS",
      "요청이 성공했습니다.",
      data
    );
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>(
      STATUS_SUCCESS,
      HttpStatus.OK.value(),
      "SUCCESS",
      message,
      data
    );
  }

  public static <T> ApiResponse<T> created(T data) {
    return new ApiResponse<>(
      STATUS_SUCCESS,
      HttpStatus.CREATED.value(),
      "CREATED",
      "리소스가 생성되었습니다.",
      data
    );
  }

  public static <T> ApiResponse<T> error(ErrorCode errorCode) {
    return new ApiResponse<>(
      STATUS_ERROR,
      errorCode.getStatus().value(),
      errorCode.getCode(),
      errorCode.getMessage(),
      null
    );
  }

  public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
    return new ApiResponse<>(
      STATUS_ERROR,
      errorCode.getStatus().value(),
      errorCode.getCode(),
      errorCode.getMessage(),
      data
    );
  }

  public static <T> ApiResponse<T> error(ErrorCode errorCode, String customMessage, T data) {
    return new ApiResponse<>(
      STATUS_ERROR,
      errorCode.getStatus().value(),
      errorCode.getCode(),
      customMessage,
      data
    );
  }

  public static <T> ApiResponse<T> accepted(String message) {
    return new ApiResponse<>(STATUS_SUCCESS, HttpStatus.ACCEPTED.value(), "ACCEPTED", message, null);
  }
}