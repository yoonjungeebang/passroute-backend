package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.debate.entity.DebateRound;
import passroutebackend.debate.entity.DebateStance;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DebateRebuttalRequest {

  private String topicTitle;
  private DebateStance stance;
  private String difficulty;
  private PersonaPayload persona;
  private DebateRound rebuttalRound;        // REBUTTAL_1 / REBUTTAL_2 (다른 값은 컨트롤러 레벨에서 검증)
  private String opponentLatestTurn;
  private List<DebateTurnItem> history;
}
