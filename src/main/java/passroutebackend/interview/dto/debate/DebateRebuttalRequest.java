package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DebateRebuttalRequest {

  private String topicTitle;
  private String stance;
  private String difficulty;
  private PersonaPayload persona;
  private String rebuttalRound;        // REBUTTAL_1 / REBUTTAL_2
  private String opponentLatestTurn;
  private List<DebateTurnItem> history;
}
