package passroutebackend.debate.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * REBUTTAL_1 후 사용자 분기 선택.
 *
 * <ul>
 *   <li>REBUT_AGAIN(반박 한 번 더): REBUTTAL_2 라운드로 진행.</li>
 *   <li>FINISH(토론 마무리): 바로 마무리(CLOSING)로 진행.</li>
 * </ul>
 *
 * <p>FE는 소문자(rebut_again/finish)로 주고받는다. 변환은 enum의 Jackson 경계에서 처리.
 */
public enum DebateBranchChoice {
  REBUT_AGAIN,
  FINISH;

  @JsonValue
  public String toJson() {
    return name().toLowerCase();
  }

  @JsonCreator
  public static DebateBranchChoice fromJson(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return DebateBranchChoice.valueOf(value.trim().toUpperCase());
  }
}
