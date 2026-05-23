package passroutebackend.schedule.entity;

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
@Table(name = "interview_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 100)
    private String companyName;

    @Column(length = 100)
    private String jobPosition;

    @Column(nullable = false)
    private LocalDateTime interviewDate;

    @Column(length = 200)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public InterviewSchedule(Long userId, String title, String companyName, String jobPosition,
                             LocalDateTime interviewDate, String location, String memo,
                             ScheduleStatus status) {
        this.userId = userId;
        this.title = title;
        this.companyName = companyName;
        this.jobPosition = jobPosition;
        this.interviewDate = interviewDate;
        this.location = location;
        this.memo = memo;
        this.status = status;
    }

    public void update(String title, String companyName, String jobPosition,
                       LocalDateTime interviewDate, String location, String memo) {
        this.title = title;
        this.companyName = companyName;
        this.jobPosition = jobPosition;
        this.interviewDate = interviewDate;
        this.location = location;
        this.memo = memo;
    }

    public void updateStatus(ScheduleStatus status) {
        this.status = status;
    }
}
