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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_competitors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiCompetitor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id", nullable = false, unique = true)
  private DebateSession session;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "persona_id", nullable = false)
  private AiPersona persona;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private DebateStance stance;

  @Builder
  public AiCompetitor(DebateSession session, AiPersona persona, DebateStance stance) {
    this.session = session;
    this.persona = persona;
    this.stance = stance;
  }
}
