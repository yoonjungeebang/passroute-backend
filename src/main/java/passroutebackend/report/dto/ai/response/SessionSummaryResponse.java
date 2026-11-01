package passroutebackend.report.dto.ai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import passroutebackend.report.dto.ai.summary.SessionSummaryContent;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SessionSummaryResponse {

  @JsonProperty("session_summary")
  private SessionSummaryContent sessionSummary;
}
