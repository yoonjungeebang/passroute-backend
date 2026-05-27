package passroutebackend.report.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpcomingReportResponse {

  private List<UpcomingReportItem> items;
}
