package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SessionSummaryRequest {

  @JsonProperty("job_title")
  private String jobTitle;

  @JsonProperty("company_name")
  private String companyName;

  @JsonProperty("per_question_summaries")
  private List<QuestionSummaryItem> perQuestionSummaries;

  @JsonProperty("item_averages")
  private ItemAverages itemAverages;

  @JsonProperty("session_score")
  private SessionScore sessionScore;

  @JsonProperty("best_q")
  private BestWorstQ bestQuestion;

  @JsonProperty("worst_q")
  private BestWorstQ worstQuestion;
}
