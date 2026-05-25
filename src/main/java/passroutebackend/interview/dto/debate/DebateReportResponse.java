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
public class DebateReportResponse {

  private String overall;
  private String strengths;
  private List<DebateWeaknessItem> weaknesses;
  private String improvements;
  private List<DebateTurnFeedback> turnFeedback;
  private String strategyAnalysis;
  private List<String> recommendedTopics;
  private String finalAdvice;
  private String debateReadinessComment;
}
