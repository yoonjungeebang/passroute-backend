package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 서버 {@code POST /report/self-intro/generate} 응답.
 */
@Getter
@NoArgsConstructor
public class SelfIntroSummaryAiResponse {

  @JsonProperty("self_intro_summary")
  private SelfIntroSummary selfIntroSummary;

  @Getter
  @NoArgsConstructor
  public static class SelfIntroSummary {
    /** 추세 진단 포함 종합 피드백 본문 */
    private String overall;

    /** 회차 간 반복 약점 진단 */
    @JsonProperty("repeated_weakness")
    private String repeatedWeakness;

    /** 다음 연습 방향 제안 */
    @JsonProperty("next_steps")
    private String nextSteps;
  }
}
