package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReportGenerationRequest {

  @JsonProperty("job_title")
  private String jobTitle;

  @JsonProperty("company_name")
  private String companyName;

  @JsonProperty("question_evaluations")
  private List<QuestionEvaluationForReport> questionEvaluations;

  @JsonProperty("session_result")
  private SessionResult sessionResult;
}
