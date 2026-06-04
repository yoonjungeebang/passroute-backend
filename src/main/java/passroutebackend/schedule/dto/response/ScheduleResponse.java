package passroutebackend.schedule.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.schedule.entity.InterviewSchedule;
import passroutebackend.schedule.entity.ScheduleStatus;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ScheduleResponse {

    private Long id;
    private Long selfIntroId;
    private String title;
    private String companyName;
    private String jobPosition;
    private LocalDateTime interviewDate;
    private String location;
    private String memo;
    private ScheduleStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ScheduleResponse from(InterviewSchedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getSelfIntroId(),
                schedule.getTitle(),
                schedule.getCompanyName(),
                schedule.getJobPosition(),
                schedule.getInterviewDate(),
                schedule.getLocation(),
                schedule.getMemo(),
                schedule.getStatus(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }
}
