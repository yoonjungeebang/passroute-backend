package passroutebackend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // Global
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G001", "서버 내부 오류가 발생했습니다."),
  INVALID_INPUT(HttpStatus.BAD_REQUEST, "G002", "유효하지 않은 입력입니다."),
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "G003", "요청한 리소스를 찾을 수 없습니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "G004", "지원하지 않는 HTTP 메서드입니다."),

  // Auth
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다."),
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "만료된 토큰입니다."),
  INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "유효하지 않은 액세스 토큰입니다."),
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A005", "유효하지 않은 리프레시 토큰입니다."),
  INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "A006", "비밀번호가 일치하지 않습니다."),

  // User
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 유저입니다."),
  USER_DELETED(HttpStatus.NOT_FOUND, "U002", "탈퇴 처리된 유저입니다."),
  DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U003", "이미 사용중인 이메일입니다."),

  // Interview
  SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "I001", "면접 세션을 찾을 수 없습니다."),
  QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "I002", "면접 질문을 찾을 수 없습니다."),
  SESSION_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "I003", "이미 종료된 면접 세션입니다."),
  ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "I004", "면접 답변을 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}