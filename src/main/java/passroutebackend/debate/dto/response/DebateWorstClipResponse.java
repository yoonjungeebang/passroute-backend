package passroutebackend.debate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "토론 최악 클립 응답")
@Getter
@AllArgsConstructor
public class DebateWorstClipResponse {

  @Schema(description = "영상 URL")
  private final String videoUrl;
  @Schema(description = "클립 선정 사유")
  private final String clipReason;
}
