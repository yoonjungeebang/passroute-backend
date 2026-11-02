package passroutebackend.debate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.debate.dto.ai.common.DebateTurnFeedback;
import passroutebackend.debate.dto.ai.common.DebateWeaknessItem;
import passroutebackend.report.dto.ai.summary.FaceAnalysisSummary;
import passroutebackend.report.dto.ai.summary.VoiceAnalysisSummary;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GET /sessions/{id}/report 응답.
 * DebateReport 엔티티의 JSON 컬럼들을 파싱해 클라이언트에 친화적으로 노출.
 */
@Schema(description = "토론 리포트 응답")
@Getter
@Builder
@AllArgsConstructor
public class DebateReportApiResponse {

  @Schema(description = "세션 ID")
  private Long sessionId;
  @Schema(description = "세션 총점")
  private Double sessionScore;
  @Schema(description = "종합 평가")
  private String overall;
  @Schema(description = "강점")
  private String strengths;
  @Schema(description = "약점 목록")
  private List<DebateWeaknessItem> weaknesses;
  @Schema(description = "개선 사항")
  private String improvements;
  @Schema(description = "턴별 피드백")
  private List<DebateTurnFeedback> turnFeedback;
  @Schema(description = "전략 분석")
  private String strategyAnalysis;
  @Schema(description = "추천 주제")
  private List<String> recommendedTopics;
  @Schema(description = "최종 조언")
  private String finalAdvice;
  @Schema(description = "토론 준비도 코멘트")
  private String debateReadinessComment;
  @Schema(description = "음성 분석 요약")
  private VoiceAnalysisSummary voiceAnalysis;
  @Schema(description = "표정 분석 요약")
  private FaceAnalysisSummary faceAnalysis;
  @Schema(description = "생성 일시")
  private LocalDateTime createdAt;
}
