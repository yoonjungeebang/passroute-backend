package passroutebackend.debate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.debate.entity.DebateBranchChoice;
import passroutebackend.debate.entity.DebateMode;
import passroutebackend.debate.entity.DebateState;

import java.util.List;

/**
 * GET /sessions/{id}/state 응답. 클라이언트가 폴링으로 사용.
 * version은 디버깅·로깅용 (클라가 보낼 필요 없음).
 */
@Schema(description = "토론 상태 응답")
@Getter
@Builder
@AllArgsConstructor
public class DebateStateResponse {

  @Schema(description = "세션 ID")
  private Long sessionId;
  @Schema(description = "토론 모드")
  private DebateMode mode;
  @Schema(description = "준비 시간 (초)")
  private int prepSeconds;
  @Schema(description = "현재 상태")
  private DebateState currentState;
  @Schema(description = "사용자 대기 여부")
  private boolean isWaitingForUser;

  @Schema(description = "분기 선택 대기 여부")
  private boolean awaitingDecision;
  @Schema(description = "가능한 분기 선택지")
  private List<DebateBranchChoice> availableChoices;

  @Schema(description = "버전 (디버깅용)")
  private Long version;
  @Schema(description = "최근 턴 목록")
  private List<DebateTurnSummary> latestTurns;
  @Schema(description = "사회자 영상 정보")
  private PersonaVideoResponse moderator;
  @Schema(description = "상대방 영상 정보")
  private PersonaVideoResponse opponent;
}
