package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SessionSummaryResponse {

  @JsonProperty("session_summary")
  private SessionSummaryContent sessionSummary;
}
