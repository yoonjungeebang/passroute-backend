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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "debate_turns")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DebateTurn {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id", nullable = false)
  private DebateSession session;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SpeakerType speakerType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "competitor_id")
  private AiCompetitor competitor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private TurnStance stance;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DebateRound round;

  @Column(nullable = false, columnDefinition = "LONGTEXT")
  private String content;

  // 사용자 턴 평가 결과 (AI 턴은 null)
  @Column
  private Double weightedScore;

  @Column(columnDefinition = "TEXT")
  private String llmScoresJson;

  @Column(columnDefinition = "TEXT")
  private String evalSummaryJson;

  @Column
  private LocalDateTime evaluatedAt;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @Builder
  public DebateTurn(DebateSession session, SpeakerType speakerType, AiCompetitor competitor,
      TurnStance stance, DebateRound round, String content) {
    if (speakerType == SpeakerType.AI_COMPETITOR && competitor == null) {
      throw new IllegalArgumentException("AI_COMPETITOR speaker must have a competitor.");
    }
    this.session = session;
    this.speakerType = speakerType;
    this.competitor = competitor;
    this.stance = stance;
    this.round = round;
    this.content = content;
  }

  public void updateEvaluation(Double weightedScore, String llmScoresJson, String evalSummaryJson) {
    this.weightedScore = weightedScore;
    this.llmScoresJson = llmScoresJson;
    this.evalSummaryJson = evalSummaryJson;
    this.evaluatedAt = LocalDateTime.now();
  }
}
