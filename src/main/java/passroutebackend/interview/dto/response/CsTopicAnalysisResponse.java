package passroutebackend.interview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.interview.entity.CsTopic;

import java.util.List;

@Schema(description = "CS 토픽 분석 응답")
@Getter
@AllArgsConstructor
public class CsTopicAnalysisResponse {

  @Schema(description = "전체 토픽 통계")
  private List<TopicStat> topics;
  @Schema(description = "취약 토픽 통계")
  private List<TopicStat> weakTopics;

  @Schema(description = "토픽별 통계")
  @Getter
  @AllArgsConstructor
  public static class TopicStat {
    @Schema(description = "CS 토픽")
    private CsTopic topic;
    @Schema(description = "관련 질문 수")
    private long questionCount;
    @Schema(description = "평균 점수 (%)")
    private double averagePercentage;
    @Schema(description = "추천 사항")
    private String recommendation;
  }
}
