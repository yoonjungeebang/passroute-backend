package passroutebackend.debate.entity;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import passroutebackend.interview.entity.Difficulty;

import java.time.LocalDateTime;

@Entity
@Table(name = "debate_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DebateSession {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "topic_id", nullable = false)
  private DebateTopic topic;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private DebateStance userStance;

  @OneToOne(mappedBy = "session", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private AiCompetitor aiCompetitor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Difficulty difficulty;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private DebateState currentState;

  @CreationTimestamp
  private LocalDateTime createdAt;

  private LocalDateTime endedAt;

  @Version
  private Long version;

  @Builder
  public DebateSession(Long userId, DebateTopic topic, DebateStance userStance,
      Difficulty difficulty) {
    this.userId = userId;
    this.topic = topic;
    this.userStance = userStance;
    this.difficulty = difficulty;
    this.currentState = DebateState.CREATED;
  }

  public void transitionTo(DebateState nextState) {
    this.currentState = nextState;
    if (nextState == DebateState.FINISHED) {
      this.endedAt = LocalDateTime.now();
    }
  }

  void assignCompetitor(AiCompetitor competitor) {
    this.aiCompetitor = competitor;
  }
}
