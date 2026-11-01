package passroutebackend.debate.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import passroutebackend.debate.entity.DebateMode;
import passroutebackend.debate.entity.DebateStance;
import passroutebackend.global.type.Difficulty;

@Schema(description = "토론 세션 생성 요청")
@Getter
@NoArgsConstructor
public class DebateSessionCreateRequest {

  @Schema(description = "토론 주제 ID")
  @NotNull
  private Long topicId;

  @Schema(description = "사용자 입장")
  @NotNull
  private DebateStance userStance;

  @Schema(description = "AI 페르소나 ID")
  @NotNull
  private Long personaId;

  @Schema(description = "난이도")
  @NotNull
  private Difficulty difficulty;

  @Schema(description = "토론 모드")
  @NotNull
  private DebateMode mode;

  @Schema(description = "자소서 ID (선택)")
  private Long introId;
}
