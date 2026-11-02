package passroutebackend.interview.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "최악 클립 저장 요청")
@Getter
@AllArgsConstructor
public class SaveWorstClipRequest {

  @Schema(description = "질문 ID")
  @NotNull
  private final Long questionId;

  @Schema(description = "영상 URL")
  @NotBlank
  private final String videoUrl;

  @Schema(description = "클립 점수")
  @NotNull
  private final Double clipScore;

  @Schema(description = "클립 선정 사유")
  private final String clipReason;
}
