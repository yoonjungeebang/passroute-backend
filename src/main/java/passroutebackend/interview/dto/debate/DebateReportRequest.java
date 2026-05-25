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
public class DebateReportRequest {

  private String topicTitle;
  private DebateStance userStance;
  private String difficulty;
  private String personaName;
  private List<DebateTurnEvalItem> turnEvaluations;
  private DebateSessionSummaryResponse sessionSummary;
}
