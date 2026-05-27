package passroutebackend.report.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReportListResponse {

  private List<ReportListItem> items;
  private int page;
  private int size;
  private long total;
}
