package passroutebackend.report.dto.ai.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import passroutebackend.report.dto.ai.score.QuestionEvaluationForReport;
import passroutebackend.report.dto.ai.score.SessionResult;

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
