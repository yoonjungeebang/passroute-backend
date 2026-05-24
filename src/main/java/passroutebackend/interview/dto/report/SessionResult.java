package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SessionResult {

  private double percentage;

  @JsonProperty("consistency_score")
  private double consistencyScore;

  @JsonProperty("item_averages")
  private ItemAverages itemAverages;

  @JsonProperty("key_weakness")
  private List<String> keyWeakness;

  @JsonProperty("interview_readiness")
  private ReadinessForReport interviewReadiness;
}
