package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import passroutebackend.debate.entity.TopicCategory;

import java.util.List;

/**
 * AI 서버 POST /debate/topics/suggest 응답.
 * news_count는 참고용(0이어도 정상, 폴백 생성됨).
 */
@Getter
@Setter
@NoArgsConstructor
public class TopicSuggestAiResponse {

  private List<Candidate> candidates;

  @JsonProperty("news_count")
  private Integer newsCount;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Candidate {
    private String title;
    private String description;
    private TopicCategory category;
  }
}