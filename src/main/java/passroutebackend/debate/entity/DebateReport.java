package passroutebackend.debate.entity;

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
import passroutebackend.interview.entity.ReportStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "debate_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DebateReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id", nullable = false, unique = true)
  private DebateSession session;

  @Column
  private Double sessionScore;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReportStatus reportStatus;

  @Column(columnDefinition = "TEXT")
  private String overall;

  @Column(columnDefinition = "TEXT")
  private String strengths;

  @Column(columnDefinition = "TEXT")
  private String weaknesses;

  @Column(columnDefinition = "TEXT")
  private String improvements;

  @Column(columnDefinition = "TEXT")
  private String turnFeedback;

  @Column(columnDefinition = "TEXT")
  private String strategyAnalysis;

  @Column(columnDefinition = "TEXT")
  private String recommendedTopics;

  @Column(columnDefinition = "TEXT")
  private String finalAdvice;

  @Column(columnDefinition = "TEXT")
  private String debateReadinessComment;

  // 음성 분석 집계
  private Double voiceScore;
  private Double avgWpm;
  private Double avgSilenceDuration;
  private Integer fillerCount;

  // 표정 분석 집계
  private Double faceScore;
  private Double avgGazeRatio;
  private Integer gazeOffCount;
  private Double avgBlinkPerMin;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @Builder
  public DebateReport(DebateSession session, Double sessionScore, ReportStatus reportStatus,
      String overall, String strengths, String weaknesses, String improvements,
      String turnFeedback, String strategyAnalysis, String recommendedTopics,
      String finalAdvice, String debateReadinessComment,
      Double voiceScore, Double avgWpm, Double avgSilenceDuration, Integer fillerCount,
      Double faceScore, Double avgGazeRatio, Integer gazeOffCount, Double avgBlinkPerMin) {
    this.session = session;
    this.sessionScore = sessionScore;
    this.reportStatus = reportStatus;
    this.overall = overall;
    this.strengths = strengths;
    this.weaknesses = weaknesses;
    this.improvements = improvements;
    this.turnFeedback = turnFeedback;
    this.strategyAnalysis = strategyAnalysis;
    this.recommendedTopics = recommendedTopics;
    this.finalAdvice = finalAdvice;
    this.debateReadinessComment = debateReadinessComment;
    this.voiceScore = voiceScore;
    this.avgWpm = avgWpm;
    this.avgSilenceDuration = avgSilenceDuration;
    this.fillerCount = fillerCount;
    this.faceScore = faceScore;
    this.avgGazeRatio = avgGazeRatio;
    this.gazeOffCount = gazeOffCount;
    this.avgBlinkPerMin = avgBlinkPerMin;
  }
}
