package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 세션 요약/리포트 요청 시 라운드별 평가 결과를 묶어 전달하는 항목.
 */
@Getter
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DebateTurnEvalItem {

  private String roundType;
  private String userContent;
  private Double weightedScore;
  private DebateTurnEvalSummary summary;
}
