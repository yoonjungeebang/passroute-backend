package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PerQuestionSummary {

  private String question;
  private String answer;
  private Double percentage;

  @JsonProperty("star_score")
  private Integer starScore;
}
