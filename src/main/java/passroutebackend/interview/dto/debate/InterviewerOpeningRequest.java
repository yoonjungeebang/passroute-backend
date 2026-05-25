package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.debate.entity.DebateStance;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class InterviewerOpeningRequest {

  private String topicTitle;
  private String topicDescription;
  private DebateStance userStance;
  private DebateStance aiStance;
  private String difficulty;
  private List<String> proKeyPoints;
  private List<String> conKeyPoints;
}
