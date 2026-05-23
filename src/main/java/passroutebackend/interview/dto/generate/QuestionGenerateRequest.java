package passroutebackend.interview.dto.generate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
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

  @JsonProperty("self_intro_items")
  private final List<SelfIntroItemDto> selfIntroItems;
}
