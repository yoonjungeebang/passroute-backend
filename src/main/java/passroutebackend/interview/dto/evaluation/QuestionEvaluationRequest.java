package passroutebackend.interview.dto.evaluation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class QuestionEvaluationRequest {

  @JsonProperty("job_title")
  private final String jobTitle;

  @JsonProperty("company_name")
  private final String companyName;

  @JsonProperty("jd_keywords")
  private final List<String> jdKeywords;

  @JsonProperty("question_type")
  private final String questionType;

  private final String question;

  private final String answer;
}
