package passroutebackend.report.dto.ai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import passroutebackend.report.dto.ai.score.StarEvaluationDetail;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StarEvaluationResponse {

  @JsonProperty("star_evaluation")
  private StarEvaluationDetail starEvaluation;
}
