package passroutebackend.schedule.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import passroutebackend.schedule.entity.ScheduleStatus;

@Getter
@NoArgsConstructor
public class ScheduleStatusUpdateRequest {

    @NotNull
    private ScheduleStatus status;
}
