package passroutebackend.debate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.debate.entity.DebateMode;

@Schema(description = "토론 세션 생성 응답")
@Getter
@Builder
@AllArgsConstructor
public class DebateSessionCreateResponse {

  @Schema(description = "세션 ID")
  private Long sessionId;
  @Schema(description = "토론 모드")
  private DebateMode mode;
  @Schema(description = "준비 시간 (초)")
  private int prepSeconds;
  @Schema(description = "사회자 영상 정보")
  private PersonaVideoResponse moderator;
  @Schema(description = "상대방 영상 정보")
  private PersonaVideoResponse opponent;
}
