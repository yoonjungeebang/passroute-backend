package passroutebackend.interview.dto.evaluation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StarEvaluationResponse {

  @JsonProperty("star_score")
  private Integer starScore;

  private String feedback;
}
