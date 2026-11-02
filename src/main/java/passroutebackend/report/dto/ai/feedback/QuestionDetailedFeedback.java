package passroutebackend.report.dto.ai.feedback;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문항별 구조화 피드백(/report/generate 응답의 detailed_feedback).
 * 기존 문자열 {@code feedback}은 그대로 두고, 이 객체가 별도로 추가된다.
 *
 * <p>improvementExample/suggestedAnswer/retryStrategy는 percentage &lt; 60 문항에서 채워지고,
 * 60 이상이면 null일 수 있다. 모든 신규 필드는 nullable이라 null-safe로 다룬다.
 */
@Schema(description = "문항별 구조화 상세 피드백")
@Getter
@NoArgsConstructor
public class QuestionDetailedFeedback {

  @Schema(description = "강점", example = "핵심 개념을 정확히 설명했습니다.")
  @JsonProperty("strength")
  private String strength = "";

  @Schema(description = "약점", example = "구체적인 사례가 부족합니다.")
  @JsonProperty("weakness")
  private String weakness = "";

  @Schema(description = "누락된 정보 목록")
  @JsonProperty("missing_info")
  @JsonAlias("missingInfo")
  private List<String> missingInfo = new ArrayList<>();

  @Schema(description = "개선 예시 (점수 60 미만 시 제공)")
  @JsonProperty("improvement_example")
  @JsonAlias("improvementExample")
  private String improvementExample;

  @Schema(description = "추천 답변 (점수 60 미만 시 제공)")
  @JsonProperty("suggested_answer")
  @JsonAlias("suggestedAnswer")
  private String suggestedAnswer;

  @Schema(description = "재시도 전략 (점수 60 미만 시 제공)")
  @JsonProperty("retry_strategy")
  @JsonAlias("retryStrategy")
  private String retryStrategy;

  // 누락 방어: AI가 명시적 null을 보내면 초기값("")이 덮어써질 수 있으므로
  // 문자열은 빈 문자열, 배열은 빈 리스트로 보장한다.
  public void normalize() {
    if (strength == null) {
      strength = "";
    }
    if (weakness == null) {
      weakness = "";
    }
    if (missingInfo == null) {
      missingInfo = new ArrayList<>();
    }
  }
}