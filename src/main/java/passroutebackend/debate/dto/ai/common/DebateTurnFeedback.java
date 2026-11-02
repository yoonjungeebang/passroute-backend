package passroutebackend.debate.dto.ai.common;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import passroutebackend.debate.entity.DebateRound;

@Schema(description = "토론 턴별 피드백")
@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DebateTurnFeedback {

  @Schema(description = "라운드 유형")
  private DebateRound roundType;
  @Schema(description = "가중 점수", example = "82.5")
  private Double weightedScore;
  @Schema(description = "피드백 내용")
  private String feedback;
}
