package passroutebackend.report.dto.ai.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StarEvaluationRequest {

  private final String question;
  private final String answer;
}
