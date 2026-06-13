package passroutebackend.debate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import passroutebackend.interview.entity.Difficulty;

@Entity
@Table(name = "ai_personas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiPersona {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String personaKey;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String background;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DebateStyle debateStyle;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Difficulty difficulty;

  @Column(columnDefinition = "TEXT")
  private String strengths;

  @Column(columnDefinition = "TEXT")
  private String weaknesses;

  @Column(columnDefinition = "TEXT")
  private String systemPromptTemplate;

  @Column(length = 1024)
  private String speakingVideoUrl;

  @Column(length = 1024)
  private String silenceVideoUrl;

  @Builder
  public AiPersona(String personaKey, String name, String background,
      DebateStyle debateStyle, Difficulty difficulty,
      String strengths, String weaknesses, String systemPromptTemplate,
      String speakingVideoUrl, String silenceVideoUrl) {
    this.personaKey = personaKey;
    this.name = name;
    this.background = background;
    this.debateStyle = debateStyle;
    this.difficulty = difficulty;
    this.strengths = strengths;
    this.weaknesses = weaknesses;
    this.systemPromptTemplate = systemPromptTemplate;
    this.speakingVideoUrl = speakingVideoUrl;
    this.silenceVideoUrl = silenceVideoUrl;
  }

  public void updateVideoUrls(String speakingVideoUrl, String silenceVideoUrl) {
    this.speakingVideoUrl = speakingVideoUrl;
    this.silenceVideoUrl = silenceVideoUrl;
  }
}
