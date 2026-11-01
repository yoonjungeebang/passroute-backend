package passroutebackend.report.dto.ai.score;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LlmScoreItem {

  private Double score;
  private Double weight;
  private String feedback;
}
