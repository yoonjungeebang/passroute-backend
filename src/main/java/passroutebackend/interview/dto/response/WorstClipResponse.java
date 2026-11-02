package passroutebackend.interview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "최악 클립 조회 응답")
@Getter
@AllArgsConstructor
public class WorstClipResponse {

  @Schema(description = "영상 URL")
  private final String videoUrl;
  @Schema(description = "클립 선정 사유")
  private final String clipReason;
}
