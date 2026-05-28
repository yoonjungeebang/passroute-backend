package passroutebackend.interview.dto.report;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FaceAnalysisSummary {

  private Double avgGazeRatio;
  private Integer gazeOffCount;
  private Double avgBlinkPerMin;
  private Double faceScore;
}
