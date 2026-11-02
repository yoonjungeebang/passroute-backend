package passroutebackend.report.dto.ai.summary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "음성 분석 요약")
@Getter
@AllArgsConstructor
public class VoiceAnalysisSummary {

  @Schema(description = "평균 발화 속도 (WPM)", example = "125.5")
  private Double avgWpm;
  @Schema(description = "평균 침묵 시간 (초)", example = "2.3")
  private Double avgSilenceDuration;
  @Schema(description = "추임새 횟수", example = "7")
  private Integer fillerCount;
  @Schema(description = "음성 종합 점수", example = "75.0")
  private Double voiceScore;
}
