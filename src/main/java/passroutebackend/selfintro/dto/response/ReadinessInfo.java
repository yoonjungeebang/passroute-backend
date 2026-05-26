package passroutebackend.selfintro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.interview.entity.InterviewReadiness;

@Getter
@AllArgsConstructor
public class ReadinessInfo {
  private InterviewReadiness level;
  private String comment;
}
