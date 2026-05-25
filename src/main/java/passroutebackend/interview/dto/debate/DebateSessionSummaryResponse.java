package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DebateSessionSummaryResponse {

  private String overall;
  private String strengths;
  private String improvements;
  private String strategyFeedback;
  private List<TurnHighlight> turnHighlights;
}
