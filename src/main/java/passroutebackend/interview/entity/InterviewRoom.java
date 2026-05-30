package passroutebackend.interview.entity;

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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column
  private Long siId;

  @Column
  private Long resumeId;

  @Column
  private Long portfolioId;

  @Column(length = 100)
  private String companyName;

  @Column(length = 100)
  private String jobPosition;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private InterviewType interviewType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private InterviewFormat interviewFormat;

  @Column(nullable = false, length = 20)
  private String interviewMode;    // PRACTICE | REAL

  @Column(length = 30)
  private String aiInterviewer;    // HR_MANAGER | TEAM_LEAD | EXECUTIVE | TECH_INTERVIEWER

  @Column(length = 20)
  private String aiCompetitors;    // EASY | MEDIUM | HARD

  @Column(columnDefinition = "TEXT")
  private String debateTopic;

  @Column(nullable = false)
  private int interviewCount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Difficulty difficulty;

  @Column(nullable = false)
  private int pressureLevel;

  @Column(nullable = false)
  private int followupCount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RoomStatus status;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  public void updateStatus(RoomStatus status) {
    this.status = status;
  }

  @Builder
  public InterviewRoom(Long userId, Long siId, Long resumeId, Long portfolioId,
                       String companyName, String jobPosition,
                       InterviewType interviewType, InterviewFormat interviewFormat, String interviewMode,
                       String aiInterviewer, String aiCompetitors, String debateTopic,
                       int interviewCount, Difficulty difficulty, int pressureLevel,
                       int followupCount, RoomStatus status) {
    this.userId = userId;
    this.siId = siId;
    this.resumeId = resumeId;
    this.portfolioId = portfolioId;
    this.companyName = companyName;
    this.jobPosition = jobPosition;
    this.interviewType = interviewType;
    this.interviewFormat = interviewFormat;
    this.interviewMode = interviewMode;
    this.aiInterviewer = aiInterviewer;
    this.aiCompetitors = aiCompetitors;
    this.debateTopic = debateTopic;
    this.interviewCount = interviewCount;
    this.difficulty = difficulty;
    this.pressureLevel = pressureLevel;
    this.followupCount = followupCount;
    this.status = status;
  }
}