package passroutebackend.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import passroutebackend.schedule.entity.ScheduleStatus;

@Schema(description = "면접 일정 상태 변경 요청")
@Getter
@NoArgsConstructor
public class ScheduleStatusUpdateRequest {

    @Schema(description = "변경할 상태", example = "COMPLETED")
    @NotNull
    private ScheduleStatus status;
}
