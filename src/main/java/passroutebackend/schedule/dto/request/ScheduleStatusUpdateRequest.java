package passroutebackend.schedule.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import passroutebackend.schedule.entity.ScheduleStatus;

@Getter
public class ScheduleStatusUpdateRequest {

    @NotNull
    private ScheduleStatus status;
}
