package passroutebackend.interview.dto.report;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VoiceAnalysisSummary {

  private Double avgWpm;              // nullable
  private Double avgSilenceDuration;  // nullable (초)
  private Integer fillerCount;        // nullable
  private Double score;               // nullable. MVP에서 항상 null. 후속 이슈에서 채움
}
