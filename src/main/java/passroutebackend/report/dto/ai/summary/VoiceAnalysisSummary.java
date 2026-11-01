package passroutebackend.report.dto.ai.summary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VoiceAnalysisSummary {

  private Double avgWpm;
  private Double avgSilenceDuration;
  private Integer fillerCount;
  private Double voiceScore;
}
