package passroutebackend.debate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 토론 주제 후보 추천 응답 (백엔드 → 프론트).
 * newsCount는 참고용(0이어도 정상).
 */
@Getter
@Builder
@AllArgsConstructor
public class DebateTopicSuggestResponse {

  private List<DebateTopicCandidateResponse> candidates;
  private Integer newsCount;
}