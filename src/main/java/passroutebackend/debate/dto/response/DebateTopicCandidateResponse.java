package passroutebackend.debate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.debate.entity.TopicCategory;

/**
 * 추천된 토론 주제 후보 1건 (아직 저장되지 않음, topicId 없음).
 */
@Getter
@Builder
@AllArgsConstructor
public class DebateTopicCandidateResponse {

  private String title;
  private String description;
  private TopicCategory category;
}