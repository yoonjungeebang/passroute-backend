package passroutebackend.debate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.debate.entity.DebateMode;
import passroutebackend.debate.entity.DebateState;

import java.util.List;

/**
 * GET /sessions/{id}/state 응답. 클라이언트가 폴링으로 사용.
 * version은 디버깅·로깅용 (클라가 보낼 필요 없음).
 */
@Getter
@Builder
@AllArgsConstructor
public class DebateStateResponse {

  private Long sessionId;
  private DebateMode mode;
  private int prepSeconds;
  private DebateState currentState;
  private boolean isWaitingForUser;
  private Long version;
  private List<DebateTurnSummary> latestTurns;
}
