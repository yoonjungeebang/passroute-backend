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
public class DebateSessionSummaryRequest {

  private String topicTitle;
  private String userStance;
  private String difficulty;
  private String personaName;
  private List<DebateTurnEvalItem> turnEvaluations;
  private List<String> aiCompetitorTurns;  // 백엔드가 createdAt ASC 정렬 보장
}
