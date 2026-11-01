package passroutebackend.debate.dto.ai.response;

import passroutebackend.debate.dto.ai.common.DebateTurnEvalSummary;
import passroutebackend.debate.dto.ai.common.DebateTurnScores;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DebateTurnEvalResponse {

  private DebateTurnScores scores;
  private Double weightedScore;
  private DebateTurnEvalSummary summary;
}
