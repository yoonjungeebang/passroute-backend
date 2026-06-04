package passroutebackend.interview.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "voice_analysis")
@Getter
@NoArgsConstructor
public class VoiceAnalysis {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long sessionId;

  @Column(nullable = false)
  private Long questionId;

  private Float avgWpm;

  private Float avgSilenceDuration;

  private Integer fillerCount;
}
