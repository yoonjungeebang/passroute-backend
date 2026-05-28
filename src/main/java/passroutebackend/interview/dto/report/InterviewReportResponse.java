package passroutebackend.interview.dto.report;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class InterviewReportResponse {

  private Long sessionId;
  private Double sessionScore;
  private VoiceAnalysisSummary voiceAnalysis;
  private FaceAnalysisSummary faceAnalysis;
  private String interviewReadiness;
  private Map<String, Double> itemAverages;
  private List<String> keyWeakness;
  private String overall;
  private String strengths;
  private List<WeaknessItem> weaknesses;
  private String improvements;
  private List<QuestionFeedback> questionFeedback;
  private List<String> recommendedQuestions;
  private String finalAdvice;
  private String readinessComment;
  private LocalDateTime createdAt;
}
