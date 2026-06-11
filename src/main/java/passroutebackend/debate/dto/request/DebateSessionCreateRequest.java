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

  /** 선택한 자기소개서 ID (선택). 지정 시 해당 기업명을 세션에 연결해 토론 중 참고 범위를 한정한다. */
  private Long introId;
}
