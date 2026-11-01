package passroutebackend.interview.dto.ai.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QATurn {

  private final String question;
  private final String answer;
}
