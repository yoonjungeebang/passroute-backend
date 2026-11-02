package passroutebackend.selfintro.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "항목별 추이 데이터")
@Getter
@AllArgsConstructor
public class ItemTrendItem {
  @Schema(description = "평가 항목명", example = "논리력")
  private String item;
  @Schema(description = "첫 회차 평균 점수", example = "65.0")
  private double firstAvg;
  @Schema(description = "마지막 회차 평균 점수", example = "80.0")
  private double lastAvg;
  @Schema(description = "점수 변화량", example = "15.0")
  private double diff;
  @Schema(description = "추이 방향")
  private TrendDirection direction;
}
