package passroutebackend.debate.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 토론 면접 진행 모드.
 *
 * <ul>
 *   <li>PRACTICE(연습): 턴마다 즉시 평가 노출, 같은 라운드 재시도 허용, 준비시간 없음.</li>
 *   <li>REAL(실전): 평가는 누적만 하고 종료 시 일괄 리포트, 재시도 불가, 준비시간 부여.</li>
 * </ul>
 *
 * <p>FE는 소문자(practice/real)로 주고받는다. 변환은 이 enum의 Jackson 경계에서 한 번에 처리한다.
 * <ul>
 *   <li>요청 역직렬화: 대소문자 무관하게 정규화하여 매핑 ({@link #fromJson}).</li>
 *   <li>응답 직렬화: 항상 소문자로 내려준다 ({@link #toJson}).</li>
 *   <li>DB는 {@code @Enumerated(STRING)}이라 {@code name()}(대문자)으로 저장된다.</li>
 * </ul>
 */
public enum DebateMode {
  PRACTICE,
  REAL;

  @JsonValue
  public String toJson() {
    return name().toLowerCase();
  }

  @JsonCreator
  public static DebateMode fromJson(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return DebateMode.valueOf(value.trim().toUpperCase());
  }
}
