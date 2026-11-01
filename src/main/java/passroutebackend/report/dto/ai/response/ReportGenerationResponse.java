package passroutebackend.report.dto.ai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import passroutebackend.report.dto.ai.feedback.QuestionFeedback;
import passroutebackend.report.dto.ai.feedback.WeaknessItem;

import java.util.List;

@Getter
@NoArgsConstructor
public class ReportGenerationResponse {

  private String overall;
  private String strengths;
  private List<WeaknessItem> weaknesses;
  private String improvements;

  @JsonProperty("question_feedback")
  private List<QuestionFeedback> questionFeedback;

  @JsonProperty("recommended_questions")
  private List<String> recommendedQuestions;

  @JsonProperty("final_advice")
  private String finalAdvice;

  @JsonProperty("readiness_comment")
  private String readinessComment;
}
