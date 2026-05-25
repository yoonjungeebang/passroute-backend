package passroutebackend.interview.dto.debate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DebateScoreItemWithWeight {

  private Integer score;
  private Double weight;
  private String feedback;
}
