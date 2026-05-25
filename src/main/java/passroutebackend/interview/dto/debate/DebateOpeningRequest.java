package passroutebackend.interview.dto.debate;

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
public class DebateOpeningRequest {

  private String topicTitle;
  private String topicDescription;
  private DebateStance stance;
  private String difficulty;
  private PersonaPayload persona;
  private List<String> proKeyPoints;
  private List<String> conKeyPoints;
}
