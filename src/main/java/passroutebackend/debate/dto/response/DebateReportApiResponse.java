package passroutebackend.debate.dto.response;

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
@Getter
@Builder
@AllArgsConstructor
public class DebateReportApiResponse {

  private Long sessionId;
  private Double sessionScore;
  private String overall;
  private String strengths;
  private List<DebateWeaknessItem> weaknesses;
  private String improvements;
  private List<DebateTurnFeedback> turnFeedback;
  private String strategyAnalysis;
  private List<String> recommendedTopics;
  private String finalAdvice;
  private String debateReadinessComment;
  private VoiceAnalysisSummary voiceAnalysis;
  private FaceAnalysisSummary faceAnalysis;
  private LocalDateTime createdAt;
}
