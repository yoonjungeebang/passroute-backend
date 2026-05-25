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
public class DebateTurnEvalRequest {

  private String topicTitle;
  private String userStance;
  private String roundType;
  private String userContent;
  private String opponentPreviousTurn;   // OPENING 라운드에선 null
  private List<DebateTurnItem> history;
}
