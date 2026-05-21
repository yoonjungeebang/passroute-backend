package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SessionSummaryContent {

  private String overall;
  private String strengths;
  private String improvements;

  @JsonProperty("question_highlights")
  private List<QuestionHighlight> questionHighlights;
}
