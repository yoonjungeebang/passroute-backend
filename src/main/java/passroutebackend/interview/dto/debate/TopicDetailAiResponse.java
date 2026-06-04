package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import passroutebackend.debate.entity.TopicCategory;

import java.util.List;

/**
 * AI 서버 POST /debate/topics/detail 응답.
 * 기존 DebateTopic(title, description, proKeyPoints, conKeyPoints, category)과 1:1 매핑된다.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TopicDetailAiResponse {

  private String topicTitle;
  private TopicCategory category;
  private String topicDescription;
  private List<String> proKeyPoints;
  private List<String> conKeyPoints;
}