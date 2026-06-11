package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.debate.entity.TopicCategory;

/**
 * AI 서버 POST /debate/topics/detail 요청.
 * 선택한 후보를 찬/반 논거가 포함된 상세 주제로 확장한다.
 */
@Getter
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TopicDetailAiRequest {

  private String title;

  // 후보의 description (선택)
  private String summary;

  private TopicCategory category;

  // 지정 시 해당 기업의 크롤링 뉴스를 참고 (company_name)
  private String companyName;
}