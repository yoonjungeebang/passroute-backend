package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import passroutebackend.debate.entity.DebateRound;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DebateTurnFeedback {

  private DebateRound roundType;
  private Double weightedScore;
  private String feedback;
}
