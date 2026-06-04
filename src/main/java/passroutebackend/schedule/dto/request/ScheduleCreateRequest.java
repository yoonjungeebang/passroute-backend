package passroutebackend.schedule.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleCreateRequest {

    private Long selfIntroId;

    @Size(max = 100)
    private String title;

    @Size(max = 100)
    private String companyName;

    @Size(max = 100)
    private String jobPosition;

    @NotNull
    private LocalDateTime interviewDate;

    @Size(max = 200)
    private String location;

    private String memo;
}
