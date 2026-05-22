package passroutebackend.interview.dto.evaluation;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LlmScoreItem {

  private Double score;
  private Double weight;
  private String feedback;
}
