package passroutebackend.report.dto.ai.summary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadinessForReport {

  private String decision;
  private String reason;
}
