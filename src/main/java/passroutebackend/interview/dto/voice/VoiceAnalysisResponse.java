package passroutebackend.interview.dto.voice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VoiceAnalysisResponse {

  @JsonProperty("session_id")
  private String sessionId;

  @JsonProperty("avg_wpm")
  private Double avgWpm;

  @JsonProperty("avg_silence_duration")
  private Double avgSilenceDuration;

  @JsonProperty("total_filler_count")
  private Integer totalFillerCount;
}
