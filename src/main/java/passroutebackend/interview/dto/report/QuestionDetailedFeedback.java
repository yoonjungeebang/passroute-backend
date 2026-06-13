package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Getter
@NoArgsConstructor
public class QuestionDetailedFeedback {

  @JsonProperty("strength")
  private String strength = "";

  @JsonProperty("weakness")
  private String weakness = "";

  @JsonProperty("missing_info")
  @JsonAlias("missingInfo")
  private List<String> missingInfo = new ArrayList<>();

  @JsonProperty("improvement_example")
  @JsonAlias("improvementExample")
  private String improvementExample;

  @JsonProperty("suggested_answer")
  @JsonAlias("suggestedAnswer")
  private String suggestedAnswer;

  @JsonProperty("retry_strategy")
  @JsonAlias("retryStrategy")
  private String retryStrategy;

  // 누락 방어: 배열은 null 대신 빈 리스트로 유지.
  public void normalize() {
    if (missingInfo == null) {
      missingInfo = new ArrayList<>();
    }
  }
}