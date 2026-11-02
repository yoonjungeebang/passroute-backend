package passroutebackend.report.dto.ai.summary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "표정 분석 요약")
@Getter
@AllArgsConstructor
public class FaceAnalysisSummary {

  @Schema(description = "평균 시선 유지 비율", example = "0.85")
  private Double avgGazeRatio;
  @Schema(description = "시선 이탈 횟수", example = "3")
  private Integer gazeOffCount;
  @Schema(description = "분당 평균 눈 깜빡임 횟수", example = "15.2")
  private Double avgBlinkPerMin;
  @Schema(description = "표정 종합 점수", example = "80.0")
  private Double faceScore;
}
