package passroutebackend.interview.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.interview.entity.CsTopic;

import java.util.List;

@Getter
@AllArgsConstructor
public class CsTopicAnalysisResponse {

  private List<TopicStat> topics;
  private List<TopicStat> weakTopics;

  @Getter
  @AllArgsConstructor
  public static class TopicStat {
    private CsTopic topic;
    private long questionCount;
    private double averagePercentage;
    private String recommendation;
  }
}
