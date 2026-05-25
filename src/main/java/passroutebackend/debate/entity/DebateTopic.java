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

@Entity
@Table(name = "debate_topics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DebateTopic {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String topicKey;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TopicCategory category;

  @Column(columnDefinition = "TEXT")
  private String proKeyPoints;

  @Column(columnDefinition = "TEXT")
  private String conKeyPoints;

  @Builder
  public DebateTopic(String topicKey, String title, String description,
      TopicCategory category, String proKeyPoints, String conKeyPoints) {
    this.topicKey = topicKey;
    this.title = title;
    this.description = description;
    this.category = category;
    this.proKeyPoints = proKeyPoints;
    this.conKeyPoints = conKeyPoints;
  }
}