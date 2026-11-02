package passroutebackend.debate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 토론 주제 후보 추천 응답 (백엔드 → 프론트).
 * newsCount는 참고용(0이어도 정상).
 */
@Schema(description = "토론 주제 후보 추천 응답")
@Getter
@Builder
@AllArgsConstructor
public class DebateTopicSuggestResponse {

  @Schema(description = "추천 후보 목록")
  private List<DebateTopicCandidateResponse> candidates;
  @Schema(description = "참고 뉴스 수")
  private Integer newsCount;
}