package passroutebackend.debate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.debate.entity.DebateRound;
import passroutebackend.debate.entity.SpeakerType;
import passroutebackend.debate.entity.TurnStance;

import java.time.LocalDateTime;

/**
 * GET /state 응답의 latestTurns 항목.
 */
@Schema(description = "토론 턴 요약")
@Getter
@Builder
@AllArgsConstructor
public class DebateTurnSummary {

  @Schema(description = "턴 ID")
  private Long id;
  @Schema(description = "발화자 타입")
  private SpeakerType speakerType;
  @Schema(description = "라운드")
  private DebateRound round;
  @Schema(description = "입장")
  private TurnStance stance;
  @Schema(description = "발화 내용")
  private String content;
  @Schema(description = "음성 URL")
  private String audioUrl;
  @Schema(description = "생성 일시")
  private LocalDateTime createdAt;

  @Schema(description = "가중 점수 (PRACTICE 모드)")
  private Double weightedScore;
  @Schema(description = "강점 평가 (PRACTICE 모드)")
  private String evalStrengths;
  @Schema(description = "개선점 평가 (PRACTICE 모드)")
  private String evalImprovements;
}
