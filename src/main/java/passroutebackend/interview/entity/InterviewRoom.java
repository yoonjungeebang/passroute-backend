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

  @Column(length = 100)
  private String jobPosition;

  @Column(length = 100)
  private String companyName;

  @Column(nullable = false)
  private int interviewCount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private InterviewType interviewType;

  @Column(length = 50)
  private String aiInterviewer;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Difficulty difficulty;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RoomStatus status;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @Builder
  public InterviewRoom(Long userId, String jobPosition, String companyName,
      int interviewCount, InterviewType interviewType, String aiInterviewer,
      Difficulty difficulty, RoomStatus status) {
    this.userId = userId;
    this.jobPosition = jobPosition;
    this.companyName = companyName;
    this.interviewCount = interviewCount;
    this.interviewType = interviewType;
    this.aiInterviewer = aiInterviewer;
    this.difficulty = difficulty;
    this.status = status;
  }
}
