package passroutebackend.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import passroutebackend.report.dto.ai.summary.FaceAnalysisSummary;
import passroutebackend.report.dto.ai.feedback.QuestionFeedback;
import passroutebackend.report.dto.ai.summary.VoiceAnalysisSummary;
import passroutebackend.report.dto.ai.feedback.WeaknessItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "면접 리포트 상세 응답")
@Getter
@Builder
public class InterviewReportResponse {

  @Schema(description = "세션 ID", example = "1")
  private Long sessionId;
  @Schema(description = "세션 종합 점수", example = "78.5")
  private Double sessionScore;
  @Schema(description = "음성 분석 요약")
  private VoiceAnalysisSummary voiceAnalysis;
  @Schema(description = "표정 분석 요약")
  private FaceAnalysisSummary faceAnalysis;
  @Schema(description = "면접 준비도", example = "GOOD")
  private String interviewReadiness;
  @Schema(description = "항목별 평균 점수", example = "{\"논리력\": 80.0, \"전달력\": 75.0}")
  private Map<String, Double> itemAverages;
  @Schema(description = "핵심 약점 목록")
  private List<String> keyWeakness;
  @Schema(description = "종합 평가")
  private String overall;
  @Schema(description = "강점 요약")
  private String strengths;
  @Schema(description = "약점 목록")
  private List<WeaknessItem> weaknesses;
  @Schema(description = "개선 방향")
  private String improvements;
  @Schema(description = "질문별 피드백")
  private List<QuestionFeedback> questionFeedback;
  @Schema(description = "추천 질문 목록")
  private List<String> recommendedQuestions;
  @Schema(description = "최종 조언")
  private String finalAdvice;
  @Schema(description = "준비도 코멘트")
  private String readinessComment;
  @Schema(description = "리포트 생성일시")
  private LocalDateTime createdAt;
}
