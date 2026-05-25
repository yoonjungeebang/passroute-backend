package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.debate.entity.DebateRound;
import passroutebackend.debate.entity.SpeakerType;
import passroutebackend.debate.entity.TurnStance;

@Getter
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DebateTurnItem {

  private SpeakerType speakerType;
  private DebateRound roundType;
  private TurnStance stance;
  private String content;
}
