package passroutebackend.debate.dto.ai.common;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import passroutebackend.debate.dto.ai.type.HighlightType;
import passroutebackend.debate.entity.DebateRound;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TurnHighlight {

  private DebateRound roundType;
  private HighlightType highlightType;
  private String comment;
}
