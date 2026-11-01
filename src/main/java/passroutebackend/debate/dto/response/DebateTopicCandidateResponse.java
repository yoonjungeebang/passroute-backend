package passroutebackend.debate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.debate.entity.TopicCategory;

/**
 * 추천된 토론 주제 후보 1건 (아직 저장되지 않음, topicId 없음).
 */
@Schema(description = "토론 주제 후보")
@Getter
@Builder
@AllArgsConstructor
public class DebateTopicCandidateResponse {

  @Schema(description = "주제 제목")
  private String title;
  @Schema(description = "주제 설명")
  private String description;
  @Schema(description = "주제 카테고리")
  private TopicCategory category;
}