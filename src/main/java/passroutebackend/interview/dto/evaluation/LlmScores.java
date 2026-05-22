package passroutebackend.interview.dto.evaluation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LlmScores {

  private LlmScoreItem relevance;
  private LlmScoreItem logic;
  private LlmScoreItem specificity;
  private LlmScoreItem conciseness;
  private LlmScoreItem clarity;
  private LlmScoreItem accuracy;
  private LlmScoreItem depth;

  @JsonProperty("job_relevance")
  private LlmScoreItem jobRelevance;

  private LlmScoreItem authenticity;
  private LlmScoreItem growth;
}