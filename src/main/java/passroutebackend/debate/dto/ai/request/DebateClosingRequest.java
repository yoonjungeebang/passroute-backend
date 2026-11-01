package passroutebackend.debate.dto.ai.request;

import passroutebackend.debate.dto.ai.common.DebateTurnItem;
import passroutebackend.debate.dto.ai.common.PersonaPayload;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.debate.entity.DebateStance;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DebateClosingRequest {

  private String topicTitle;
  private DebateStance stance;
  private String difficulty;
  private PersonaPayload persona;
  private List<DebateTurnItem> history;
}
