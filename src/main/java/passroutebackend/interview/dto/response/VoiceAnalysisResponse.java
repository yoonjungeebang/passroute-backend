package passroutebackend.interview.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "음성 분석 결과 응답 (AI 서버)")
@Getter
@NoArgsConstructor
public class VoiceAnalysisResponse {

  @Schema(description = "세션 ID", example = "42")
  @JsonProperty("session_id")
  private String sessionId;

  @Schema(description = "평균 발화 속도 (WPM)", example = "125.5")
  @JsonProperty("avg_wpm")
  private Double avgWpm;

  @Schema(description = "평균 침묵 시간 (초)", example = "2.3")
  @JsonProperty("avg_silence_duration")
  private Double avgSilenceDuration;

  @Schema(description = "총 추임새 횟수", example = "7")
  @JsonProperty("total_filler_count")
  private Integer totalFillerCount;
}
