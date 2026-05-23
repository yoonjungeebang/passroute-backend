package passroutebackend.schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ScheduleUpdateRequest {

    @NotBlank
    private String title;

    private String companyName;

    private String jobPosition;

    @NotNull
    private LocalDateTime interviewDate;

    private String location;

    private String memo;
}
