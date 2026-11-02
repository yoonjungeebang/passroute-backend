package passroutebackend.report.dto.ai.feedback;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 틀린 기술 주장 1건. (모든 필드 string, AI가 채워 보냄)
 * snake_case 기본, 혹시 모를 camelCase 응답도 alias로 수용.
 */
@Schema(description = "틀린 기술 주장 항목")
@Getter
@NoArgsConstructor
public class IncorrectClaim {

  @Schema(description = "사용자의 주장", example = "HashMap은 thread-safe합니다.")
  @JsonProperty("user_claim")
  @JsonAlias("userClaim")
  private String userClaim;

  @Schema(description = "문제점", example = "HashMap은 동기화를 지원하지 않습니다.")
  @JsonProperty("issue")
  private String issue;

  @Schema(description = "올바른 설명", example = "ConcurrentHashMap을 사용해야 thread-safe합니다.")
  @JsonProperty("correct_explanation")
  @JsonAlias("correctExplanation")
  private String correctExplanation;

  @Schema(description = "수정 제안", example = "ConcurrentHashMap 또는 Collections.synchronizedMap을 사용하세요.")
  @JsonProperty("suggested_fix")
  @JsonAlias("suggestedFix")
  private String suggestedFix;
}