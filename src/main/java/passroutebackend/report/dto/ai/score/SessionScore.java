package passroutebackend.report.dto.ai.score;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SessionScore {

  private double raw;
  private double percentage;

  @JsonProperty("consistency_score")
  private double consistencyScore;
}
