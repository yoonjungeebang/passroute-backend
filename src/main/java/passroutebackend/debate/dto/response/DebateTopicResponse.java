package passroutebackend.debate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.debate.entity.TopicCategory;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DebateTopicResponse {

  private Long id;
  private String topicKey;
  private String title;
  private String description;
  private TopicCategory category;
  private List<String> proKeyPoints;
  private List<String> conKeyPoints;
}
