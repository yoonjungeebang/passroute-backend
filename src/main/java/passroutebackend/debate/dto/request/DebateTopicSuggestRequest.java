package passroutebackend.debate.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 토론 주제 후보 추천 요청 (프론트 → 백엔드).
 * keywords는 빈 배열/생략 허용, count 미지정 시 3.
 */
@Getter
@NoArgsConstructor
public class DebateTopicSuggestRequest {

  private List<String> keywords;

  @Min(1)
  @Max(5)
  private Integer count;
}