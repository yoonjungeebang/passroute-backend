package passroutebackend.selfintro.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 자소서별 리포트의 AI 종합 피드백(3섹션).
 * AI 호출 실패 시 전체가 null → 프론트는 {@code growthSummary}로 폴백한다.
 * 프론트가 3개 섹션에 각각 매핑하므로 하나의 문자열로 합치지 않는다.
 */
@Schema(description = "AI 종합 피드백 (3섹션)")
@Getter
@AllArgsConstructor
public class AiSummary {
  @Schema(description = "종합 평가")
  private String overall;
  @Schema(description = "반복되는 약점")
  private String repeatedWeakness;
  @Schema(description = "다음 단계 제안")
  private String nextSteps;
}
