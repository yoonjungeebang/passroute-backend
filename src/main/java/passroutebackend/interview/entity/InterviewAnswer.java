package passroutebackend.interview.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "interview_answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewAnswer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", nullable = false)
  private InterviewQuestion question;

  @Column(nullable = false, columnDefinition = "LONGTEXT")
  private String answerText;

  @Column
  private Double percentage;

  @Column
  private Integer starScore;

  @Column(columnDefinition = "TEXT")
  private String llmScores;

  @CreationTimestamp
  private LocalDateTime answeredAt;

  @Builder
  public InterviewAnswer(InterviewQuestion question, String answerText) {
    this.question = question;
    this.answerText = answerText;
  }

  public void updateEvaluationResult(Double percentage, Integer starScore, String llmScores) {
    this.percentage = percentage;
    this.starScore = starScore;
    this.llmScores = llmScores;
  }
}
