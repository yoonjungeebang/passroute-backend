package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * AI 서버 POST /debate/topics/suggest 요청.
 * 크롤링 뉴스 기반으로 토론 주제 제목 후보 N개를 추천받는다.
 */
@Getter
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TopicSuggestAiRequest {

  // 빈 배열 허용
  private List<String> keywords;

  // 1~5 (기본 3)
  private Integer count;
}