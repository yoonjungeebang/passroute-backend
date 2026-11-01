package passroutebackend.debate.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 토론 주제 후보 추천 요청 (프론트 → 백엔드).
 * keywords는 빈 배열/생략 허용, count 미지정 시 3.
 */
@Schema(description = "토론 주제 후보 추천 요청")
@Getter
@NoArgsConstructor
public class DebateTopicSuggestRequest {

  @Schema(description = "키워드 목록")
  private List<String> keywords;

  @Schema(description = "추천 후보 수 (1~5, 기본 3)", example = "3")
  @Min(1)
  @Max(5)
  private Integer count;

  @Schema(description = "자소서 ID (선택)")
  private Long introId;
}