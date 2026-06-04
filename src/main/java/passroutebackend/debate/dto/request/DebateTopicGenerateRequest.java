package passroutebackend.debate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import passroutebackend.debate.entity.TopicCategory;

/**
 * 선택한 후보를 상세화하여 DB에 저장하는 요청 (프론트 → 백엔드).
 * description은 후보의 설명(요약)으로, AI 상세 생성의 summary로 전달된다.
 */
@Getter
@NoArgsConstructor
public class DebateTopicGenerateRequest {

  @NotBlank
  private String title;

  private String description;

  @NotNull
  private TopicCategory category;
}