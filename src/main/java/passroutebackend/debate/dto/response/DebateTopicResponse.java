package passroutebackend.debate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.debate.entity.TopicCategory;

import java.util.List;

@Schema(description = "토론 주제 응답")
@Getter
@Builder
@AllArgsConstructor
public class DebateTopicResponse {

  @Schema(description = "주제 ID")
  private Long id;
  @Schema(description = "주제 키")
  private String topicKey;
  @Schema(description = "주제 제목")
  private String title;
  @Schema(description = "주제 설명")
  private String description;
  @Schema(description = "주제 카테고리")
  private TopicCategory category;
  @Schema(description = "찬성 핵심 논점")
  private List<String> proKeyPoints;
  @Schema(description = "반대 핵심 논점")
  private List<String> conKeyPoints;
}
