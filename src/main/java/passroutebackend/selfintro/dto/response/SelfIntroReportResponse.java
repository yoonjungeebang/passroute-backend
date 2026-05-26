package passroutebackend.selfintro.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SelfIntroReportResponse {

  private Long selfIntroId;
  private String companyName;
  private String jobPosition;
  private int totalSessions;
  private boolean hasTrendData;

  private List<SessionScorePoint> scoreTimeline;
  private Double overallAverage;
  private Map<String, Double> itemAverages;
  private List<ItemTrendItem> itemTrend;
  private SessionSummary bestSession;
  private SessionSummary worstSession;
  private List<RecommendedQuestionCount> topRecommendedQuestions;
  private ReadinessInfo readiness;
  private String growthSummary;
}
