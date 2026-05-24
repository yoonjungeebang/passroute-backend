package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuestionSummaryItem {

  @JsonProperty("question_index")
  private int questionIndex;

  @JsonProperty("question_type")
  private String questionType;

  private String question;
  private Double percentage;
  private QuestionSummary summary;
}
