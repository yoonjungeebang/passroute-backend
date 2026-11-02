package passroutebackend.report.dto.ai.feedback;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "약점 항목")
@Getter
@NoArgsConstructor
public class WeaknessItem {

  @Schema(description = "약점 항목명", example = "논리력")
  private String item;
  @Schema(description = "약점 코멘트", example = "답변의 논리적 흐름이 부족합니다.")
  private String comment;
}
