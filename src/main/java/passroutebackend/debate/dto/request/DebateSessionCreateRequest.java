package passroutebackend.debate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import passroutebackend.debate.entity.DebateMode;
import passroutebackend.debate.entity.DebateStance;
import passroutebackend.interview.entity.Difficulty;

@Getter
@NoArgsConstructor
public class DebateSessionCreateRequest {

  @NotNull
  private Long topicId;

  @NotNull
  private DebateStance userStance;

  @NotNull
  private Long personaId;

  @NotNull
  private Difficulty difficulty;

  @NotNull
  private DebateMode mode;
}
