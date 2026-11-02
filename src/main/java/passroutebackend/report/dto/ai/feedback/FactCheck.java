package passroutebackend.report.dto.ai.feedback;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기술 사실 검증 결과. question_type == "technical"이거나 답변에 기술 주장이 있을 때 채워진다.
 * 인성/비기술 답변은 {@code factCheckApplicable=false}(또는 fact_check 자체가 null)이며,
 * 이 경우 응답 정규화에서 미적용 + 빈 배열 기본값으로 내려간다.
 */
@Schema(description = "기술 사실 검증 결과")
@Getter
@NoArgsConstructor
public class FactCheck {

  @Schema(description = "사실 검증 적용 여부 (기술 문항만 true)", example = "true")
  @JsonProperty("is_fact_check_applicable")
  @JsonAlias({"isFactCheckApplicable", "factCheckApplicable"})
  private boolean factCheckApplicable;

  @Schema(description = "틀린 주장 목록")
  @JsonProperty("incorrect_claims")
  @JsonAlias("incorrectClaims")
  private List<IncorrectClaim> incorrectClaims = new ArrayList<>();

  @Schema(description = "근거 없이 단정한 주장 목록")
  @JsonProperty("unsupported_claims")
  @JsonAlias("unsupportedClaims")
  private List<String> unsupportedClaims = new ArrayList<>();

  @Schema(description = "올바른 설명")
  @JsonProperty("correct_explanation")
  @JsonAlias("correctExplanation")
  private String correctExplanation;

  @Schema(description = "수정 제안")
  @JsonProperty("suggested_fix")
  @JsonAlias("suggestedFix")
  private String suggestedFix;

  // fact_check 자체가 없을 때 응답에 넣을 안전 기본값(미적용 + 빈 배열).
  public static FactCheck empty() {
    return new FactCheck();
  }

  // 누락 방어: 배열은 null 대신 빈 리스트로 유지.
  public void normalize() {
    if (incorrectClaims == null) {
      incorrectClaims = new ArrayList<>();
    }
    if (unsupportedClaims == null) {
      unsupportedClaims = new ArrayList<>();
    }
  }
}