package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class ReportGenerationResponse {

  private String overall;
  private String strengths;
  private List<String> weaknesses;
  private String improvements;

  @JsonProperty("question_feedback")
  private List<Map<String, Object>> questionFeedback;

  @JsonProperty("recommended_questions")
  private List<String> recommendedQuestions;

  @JsonProperty("final_advice")
  private String finalAdvice;

  @JsonProperty("readiness_comment")
  private String readinessComment;
}
