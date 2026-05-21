package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuestionHighlight {

  @JsonProperty("question_index")
  private int questionIndex;

  private String type;
  private String comment;
}
