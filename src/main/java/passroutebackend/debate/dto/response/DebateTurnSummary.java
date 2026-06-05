package passroutebackend.debate.dto.response;

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
@Getter
@Builder
@AllArgsConstructor
public class DebateTurnSummary {

  private Long id;
  private SpeakerType speakerType;
  private DebateRound round;
  private TurnStance stance;
  private String content;
  private String audioUrl;
  private LocalDateTime createdAt;

  // 사용자 턴 평가 결과. PRACTICE 모드에서만 채워지고, REAL 모드에서는 항상 null (종료 리포트로만 노출).
  private Double weightedScore;
  private String evalStrengths;
  private String evalImprovements;
}
