package passroutebackend.interview.entity;

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
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewSession {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private InterviewRoom interviewRoom;

  @Column(nullable = false)
  private int sessionNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SessionStatus status;

  @Version
  private Long version;

  private LocalDateTime startedAt;

  private LocalDateTime endedAt;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Builder
  public InterviewSession(InterviewRoom interviewRoom, int sessionNumber, SessionStatus status) {
    this.interviewRoom = interviewRoom;
    this.sessionNumber = sessionNumber;
    this.status = status;
    this.startedAt = LocalDateTime.now();
  }

  public boolean markInProgress() {
    if (this.status != SessionStatus.PREPARING) {
      return false;
    }
    this.status = SessionStatus.IN_PROGRESS;
    return true;
  }

  public boolean markPrepareFailed() {
    if (this.status != SessionStatus.PREPARING) {
      return false;
    }
    this.status = SessionStatus.PREPARE_FAILED;
    return true;
  }

  public void end(SessionStatus status) {
    this.status = status;
    this.endedAt = LocalDateTime.now();
  }

  public boolean isExpired(long timeoutMinutes) {
    return this.status == SessionStatus.PREPARING
        && this.startedAt.plusMinutes(timeoutMinutes).isBefore(LocalDateTime.now());
  }

  public void softDelete() {
    this.isActive = false;
    this.deletedAt = LocalDateTime.now();
  }
}
