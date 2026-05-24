package passroutebackend.schedule.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ScheduleCalendarResponse {

    private int year;
    private int month;
    private List<ScheduleResponse> schedules;
}
