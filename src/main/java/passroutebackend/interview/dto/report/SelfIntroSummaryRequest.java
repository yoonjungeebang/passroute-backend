package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 자소서별(cross-session) AI 종합 피드백 요청.
 * AI 서버 {@code POST /report/self-intro/generate} 로 전송한다. (필드 snake_case 직렬화)
 *
 * <p>척도 주의: itemAverages/itemTrend는 0~5, overallAverage/sessions.score는 0~100.
 */
@Getter
@Builder
@AllArgsConstructor
public class SelfIntroSummaryRequest {

  @JsonProperty("job_title")
  private String jobTitle;

  @JsonProperty("company_name")
  private String companyName;

  @JsonProperty("total_sessions")
  private int totalSessions;

  /** 0~100 */
  @JsonProperty("overall_average")
  private double overallAverage;

  /** 전 회차 항목별 평균(0~5). 키는 snake_case(job_relevance 등). */
  @JsonProperty("item_averages")
  private Map<String, Double> itemAverages;

  @JsonProperty("item_trend")
  private List<ItemTrend> itemTrend;

  /** 전 회차(round 오름차순) 점수 + 약점. */
  private List<Session> sessions;

  /** READY | NEEDS_REVIEW | NEEDS_IMPROVEMENT */
  private String readiness;

  @Getter
  @AllArgsConstructor
  public static class ItemTrend {
    private String item;

    @JsonProperty("first_avg")
    private double firstAvg;

    @JsonProperty("last_avg")
    private double lastAvg;

    /** UP | STABLE | DOWN */
    private String direction;
  }

  @Getter
  @AllArgsConstructor
  public static class Session {
    private int round;

    /** 0~100 */
    private double score;

    @JsonProperty("key_weaknesses")
    private List<String> keyWeaknesses;
  }
}
