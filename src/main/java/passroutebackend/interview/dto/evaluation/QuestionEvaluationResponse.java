package passroutebackend.interview.dto.evaluation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuestionEvaluationResponse {

  @JsonProperty("llm_scores")
  private LlmScores llmScores;

  private EvaluationSummary summary;
}
