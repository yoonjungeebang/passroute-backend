package passroutebackend.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.schedule.entity.InterviewSchedule;
import passroutebackend.schedule.entity.ScheduleStatus;

import java.time.LocalDateTime;

@Schema(description = "면접 일정 응답")
@Getter
@AllArgsConstructor
public class ScheduleResponse {

    @Schema(description = "일정 ID")
    private Long id;
    @Schema(description = "연결된 자소서 ID")
    private Long selfIntroId;
    @Schema(description = "일정 제목")
    private String title;
    @Schema(description = "기업명")
    private String companyName;
    @Schema(description = "직무")
    private String jobPosition;
    @Schema(description = "면접 일시")
    private LocalDateTime interviewDate;
    @Schema(description = "면접 장소")
    private String location;
    @Schema(description = "메모")
    private String memo;
    @Schema(description = "일정 상태")
    private ScheduleStatus status;
    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;
    @Schema(description = "수정 일시")
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
