package passroutebackend.selfintro.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Schema(description = "자소서별 면접 리포트 응답")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SelfIntroReportResponse {

  @Schema(description = "자소서 ID", example = "1")
  private Long selfIntroId;
  @Schema(description = "기업명", example = "네이버")
  private String companyName;
  @Schema(description = "직무", example = "백엔드 개발")
  private String jobPosition;
  @Schema(description = "총 면접 세션 수", example = "5")
  private int totalSessions;
  @Schema(description = "추이 데이터 존재 여부", example = "true")
  private boolean hasTrendData;

  @Schema(description = "회차별 점수 타임라인")
  private List<SessionScorePoint> scoreTimeline;
  @Schema(description = "전체 평균 점수", example = "75.3")
  private Double overallAverage;
  @Schema(description = "항목별 평균 점수", example = "{\"논리력\": 80.0, \"전달력\": 70.0}")
  private Map<String, Double> itemAverages;
  @Schema(description = "항목별 추이 목록")
  private List<ItemTrendItem> itemTrend;
  @Schema(description = "최고 점수 세션 요약")
  private SessionSummary bestSession;
  @Schema(description = "최저 점수 세션 요약")
  private SessionSummary worstSession;
  @Schema(description = "자주 추천된 질문 목록")
  private List<RecommendedQuestionCount> topRecommendedQuestions;
  @Schema(description = "면접 준비도 정보")
  private ReadinessInfo readiness;
  @Schema(description = "성장 요약 (AI 실패 시 폴백용)")
  private String growthSummary;
  @Schema(description = "AI 종합 피드백")
  private AiSummary aiSummary;
}
