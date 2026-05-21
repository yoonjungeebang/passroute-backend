package passroutebackend.interview.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StarEvaluationRequest {

  private final String question;
  private final String answer;
}
