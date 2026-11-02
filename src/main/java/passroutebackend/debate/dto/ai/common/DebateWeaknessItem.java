package passroutebackend.debate.dto.ai.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "토론 약점 항목")
@Getter
@Setter
@NoArgsConstructor
public class DebateWeaknessItem {

  @Schema(description = "약점 항목명", example = "반론 구성력")
  private String item;
  @Schema(description = "약점 코멘트", example = "상대 주장에 대한 직접적인 반박이 부족합니다.")
  private String comment;
}
