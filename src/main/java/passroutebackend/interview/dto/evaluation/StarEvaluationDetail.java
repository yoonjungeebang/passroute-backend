package passroutebackend.interview.dto.evaluation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StarEvaluationDetail {

  private boolean applicable;
  private String reason;

  @JsonProperty("star_score")
  private Integer starScore;
}
