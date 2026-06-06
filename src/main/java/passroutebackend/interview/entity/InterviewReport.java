package passroutebackend.interview.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id", nullable = false)
  private InterviewSession session;

  @Column(columnDefinition = "TEXT")
  private String overall;

  @Column(columnDefinition = "TEXT")
  private String strengths;

  @Column(columnDefinition = "TEXT")
  private String weaknesses;

  @Column(columnDefinition = "TEXT")
  private String improvements;

  @Column(columnDefinition = "TEXT")
  private String questionFeedback;

  @Column(columnDefinition = "TEXT")
  private String recommendedQuestions;

  @Column(columnDefinition = "TEXT")
  private String finalAdvice;

  @Column(columnDefinition = "TEXT")
  private String readinessComment;

  @Column
  private Double sessionScore;

  @Column
  private Double faceScore;

  @Column
  private Double avgGazeRatio;

  @Column
  private Integer gazeOffCount;

  @Column
  private Double avgBlinkPerMin;

  @Enumerated(EnumType.STRING)
  @Column(length = 30)
  private InterviewReadiness interviewReadiness;

  @Column(columnDefinition = "TEXT")
  private String keyWeakness;

  @Column(columnDefinition = "TEXT")
  private String itemAverages;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReportStatus reportStatus;

  // ── 음성 분석 통계 (nullable: AI 서버 응답 실패 / 데이터 없음 / 후속 점수화 대기) ─
  @Column
  private Double avgWpm;

  @Column
  private Double avgSilenceDuration;

  @Column
  private Integer fillerCount;

  @Column
  private Double voiceScore;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @Builder
  public InterviewReport(InterviewSession session, String overall, String strengths,
      String weaknesses, String improvements, String questionFeedback,
      String recommendedQuestions, String finalAdvice, String readinessComment,
      Double sessionScore, InterviewReadiness interviewReadiness,
      String keyWeakness, String itemAverages, ReportStatus reportStatus,
      Double voiceScore, Double faceScore,
      Double avgWpm, Double avgSilenceDuration, Integer fillerCount,
      Double avgGazeRatio, Integer gazeOffCount, Double avgBlinkPerMin) {
    this.session = session;
    this.overall = overall;
    this.strengths = strengths;
    this.weaknesses = weaknesses;
    this.improvements = improvements;
    this.questionFeedback = questionFeedback;
    this.recommendedQuestions = recommendedQuestions;
    this.finalAdvice = finalAdvice;
    this.readinessComment = readinessComment;
    this.sessionScore = sessionScore;
    this.interviewReadiness = interviewReadiness;
    this.keyWeakness = keyWeakness;
    this.itemAverages = itemAverages;
    this.reportStatus = reportStatus;
    this.voiceScore = voiceScore;
    this.faceScore = faceScore;
    this.avgWpm = avgWpm;
    this.avgSilenceDuration = avgSilenceDuration;
    this.fillerCount = fillerCount;
    this.avgGazeRatio = avgGazeRatio;
    this.gazeOffCount = gazeOffCount;
    this.avgBlinkPerMin = avgBlinkPerMin;
  }

  // GENERATING row를 완료 결과로 in-place 갱신 (호출 전 status==GENERATING 보장 + 락 필요)
  public void completeReport(Double sessionScore, InterviewReadiness interviewReadiness,
      String overall, String strengths, String weaknesses, String improvements,
      String questionFeedback, String recommendedQuestions, String finalAdvice,
      String readinessComment, String keyWeakness, String itemAverages,
      Double voiceScore, Double faceScore,
      Double avgWpm, Double avgSilenceDuration, Integer fillerCount,
      Double avgGazeRatio, Integer gazeOffCount, Double avgBlinkPerMin) {
    this.sessionScore = sessionScore;
    this.interviewReadiness = interviewReadiness;
    this.overall = overall;
    this.strengths = strengths;
    this.weaknesses = weaknesses;
    this.improvements = improvements;
    this.questionFeedback = questionFeedback;
    this.recommendedQuestions = recommendedQuestions;
    this.finalAdvice = finalAdvice;
    this.readinessComment = readinessComment;
    this.keyWeakness = keyWeakness;
    this.itemAverages = itemAverages;
    this.voiceScore = voiceScore;
    this.faceScore = faceScore;
    this.avgWpm = avgWpm;
    this.avgSilenceDuration = avgSilenceDuration;
    this.fillerCount = fillerCount;
    this.avgGazeRatio = avgGazeRatio;
    this.gazeOffCount = gazeOffCount;
    this.avgBlinkPerMin = avgBlinkPerMin;
    this.reportStatus = ReportStatus.COMPLETED;
  }

  public void markFailed() {
    this.reportStatus = ReportStatus.FAILED;
  }
}
