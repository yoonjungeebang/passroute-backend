package passroutebackend.debate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.debate.entity.DebateMode;
import passroutebackend.interview.dto.response.PersonaVideoResponse;

@Getter
@Builder
@AllArgsConstructor
public class DebateSessionCreateResponse {

  private Long sessionId;
  private DebateMode mode;
  private int prepSeconds;
  private PersonaVideoResponse moderator;
  private PersonaVideoResponse opponent;
}
