package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StarEvalForReport {

  private boolean applicable;

  @JsonProperty("star_score")
  private Integer starScore;
}
