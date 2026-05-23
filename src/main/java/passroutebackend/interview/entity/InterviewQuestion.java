package passroutebackend.interview.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "interview_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewQuestion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id", nullable = false)
  private InterviewSession session;

  @Column(nullable = false)
  private int setNumber;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String questionText;

  @Column(nullable = false)
  private int questionOrder;

  @Column(nullable = false)
  private boolean followUp;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @Builder
  public InterviewQuestion(InterviewSession session, int setNumber,
      String questionText, int questionOrder, boolean followUp) {
    this.session = session;
    this.setNumber = setNumber;
    this.questionText = questionText;
    this.questionOrder = questionOrder;
    this.followUp = followUp;
  }
}
