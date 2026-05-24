package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BestWorstQ {

  @JsonProperty("question_index")
  private int questionIndex;

  private String question;
  private Double percentage;
  private QuestionSummary summary;
}
