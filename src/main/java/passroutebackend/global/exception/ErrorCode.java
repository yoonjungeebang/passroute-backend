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
  DUPLICATE_PHONE(HttpStatus.CONFLICT, "U004", "이미 가입된 휴대폰번호입니다."),
  PHONE_NOT_REGISTERED(HttpStatus.NOT_FOUND, "U005", "가입되지 않은 휴대폰번호입니다."),
  SOCIAL_USER_NO_PASSWORD(HttpStatus.BAD_REQUEST, "U006", "소셜 로그인 계정은 비밀번호를 변경할 수 없습니다."),

  // Phone Verification
  PHONE_VERIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "P001", "인증 요청 내역이 없습니다. 인증번호를 먼저 발송해주세요."),
  PHONE_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "P002", "인증번호가 만료되었습니다. 다시 발송해주세요."),
  PHONE_VERIFICATION_INVALID_CODE(HttpStatus.BAD_REQUEST, "P003", "인증번호가 올바르지 않습니다."),
  PHONE_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "P004", "휴대폰 인증이 완료되지 않았습니다."),
  SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P005", "SMS 발송에 실패했습니다."),

  // Self Intro
  SELF_INTRO_NOT_FOUND(HttpStatus.NOT_FOUND, "SI001", "자기소개서를 찾을 수 없습니다."),

  // Document
  DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "문서를 찾을 수 없습니다."),

  // Interview
  SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "I001", "면접 세션을 찾을 수 없습니다."),
  QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "I002", "면접 질문을 찾을 수 없습니다."),
  SESSION_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "I003", "이미 종료된 면접 세션입니다."),
  ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "I004", "면접 답변을 찾을 수 없습니다."),
  ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "I005", "면접 방을 찾을 수 없습니다."),
  INVALID_INTERVIEW_FORMAT(HttpStatus.BAD_REQUEST, "I006", "MULTI 면접 시 aiCompetitors와 debateTopic은 필수입니다."),
  ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "I007", "해당 면접 방에 접근 권한이 없습니다."),
  ROOM_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "I008", "완료되지 않은 면접은 이력으로 조회할 수 없습니다."),
  REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "I009", "리포트를 찾을 수 없습니다."),
  REPORT_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "I010", "리포트 생성에 실패했습니다."),
  SESSION_NOT_ENDED(HttpStatus.BAD_REQUEST, "I011", "아직 종료되지 않은 면접 세션입니다."),
  REPORT_NO_ANSWERS(HttpStatus.UNPROCESSABLE_ENTITY, "I013", "답변 기록이 없어 리포트를 생성할 수 없습니다."),
  ANSWER_ALREADY_EXISTS(HttpStatus.CONFLICT, "I014", "이미 답변이 제출된 질문입니다."),

  // Schedule
  SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "면접 일정을 찾을 수 없습니다."),
  SCHEDULE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "S002", "해당 일정에 접근 권한이 없습니다."),

  AI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "I007", "AI 서버 호출에 실패했습니다."),

  // Debate
  DEBATE_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "DB001", "토론 세션을 찾을 수 없습니다."),
  DEBATE_TOPIC_NOT_FOUND(HttpStatus.NOT_FOUND, "DB002", "토론 주제를 찾을 수 없습니다."),
  PERSONA_NOT_FOUND(HttpStatus.NOT_FOUND, "DB003", "AI 페르소나를 찾을 수 없습니다."),
  INVALID_DEBATE_STATE(HttpStatus.BAD_REQUEST, "DB004", "잘못된 토론 상태 전이입니다."),
  DEBATE_TURN_OUT_OF_ORDER(HttpStatus.BAD_REQUEST, "DB005", "현재 사용자 턴이 아닙니다."),
  DEBATE_STT_NOT_READY(HttpStatus.CONFLICT, "DB006", "아직 인식된 발화가 없습니다. 잠시 후 다시 시도해주세요.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}