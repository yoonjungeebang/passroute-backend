package passroutebackend.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "다가오는 면접 리포트 응답")
@Getter
@Builder
@AllArgsConstructor
public class UpcomingReportResponse {

  @Schema(description = "다가오는 면접 목록")
  private List<UpcomingReportItem> items;
}
