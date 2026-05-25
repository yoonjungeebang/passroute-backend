package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 라운드별 활성 매트릭스:
 * - OPENING: rebuttalQuality·consistency null
 * - REBUTTAL_1/2: 전부
 * - CLOSING: rebuttalQuality null
 */
@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DebateTurnScores {

  private DebateScoreItemWithWeight logic;
  private DebateScoreItemWithWeight rebuttalQuality;
  private DebateScoreItemWithWeight consistency;
  private DebateScoreItemWithWeight attitude;
}
