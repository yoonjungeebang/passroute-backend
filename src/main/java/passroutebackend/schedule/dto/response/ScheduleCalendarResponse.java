package passroutebackend.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Schema(description = "면접 일정 캘린더 응답")
@Getter
@AllArgsConstructor
public class ScheduleCalendarResponse {

    @Schema(description = "연도", example = "2026")
    private int year;
    @Schema(description = "월", example = "11")
    private int month;
    @Schema(description = "해당 월 일정 목록")
    private List<ScheduleResponse> schedules;
}
