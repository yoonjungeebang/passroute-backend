package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 틀린 기술 주장 1건. (모든 필드 string, AI가 채워 보냄)
 * snake_case 기본, 혹시 모를 camelCase 응답도 alias로 수용.
 */
@Getter
@NoArgsConstructor
public class IncorrectClaim {

  @JsonProperty("user_claim")
  @JsonAlias("userClaim")
  private String userClaim;

  @JsonProperty("issue")
  private String issue;

  @JsonProperty("correct_explanation")
  @JsonAlias("correctExplanation")
  private String correctExplanation;

  @JsonProperty("suggested_fix")
  @JsonAlias("suggestedFix")
  private String suggestedFix;
}