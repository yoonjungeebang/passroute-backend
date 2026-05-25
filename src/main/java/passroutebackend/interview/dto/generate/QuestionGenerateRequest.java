package passroutebackend.interview.dto.generate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionGenerateRequest {

  @JsonProperty("interview_type")
  private final String interviewType;

  @JsonProperty("difficulty")
  private final String difficulty;

  @JsonProperty("company_name")
  private final String companyName;

  @JsonProperty("job_position")
  private final String jobPosition;

  @JsonProperty("question_count")
  private final int questionCount;

  @JsonProperty("resume")
  private final String resumeText;

  @JsonProperty("portfolio")
  private final String portfolioText;

  @JsonProperty("persona")
  private final String persona;

  @JsonProperty("pressure_level")
  private final int pressureLevel;

  @JsonProperty("followup_count")
  private final int followupCount;

  @JsonProperty("interview_format")
  private final String interviewFormat;

  @JsonProperty("cover_letter")
  private final String coverLetter;
}
