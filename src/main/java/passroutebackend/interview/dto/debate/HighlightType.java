package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 세션 요약의 turn_highlights 항목 종류.
 * AI 서버 명세상 소문자("best"/"worst")로 직렬화.
 */
public enum HighlightType {
  BEST("best"),
  WORST("worst");

  private final String value;

  HighlightType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
