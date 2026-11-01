package passroutebackend.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "리포트 목록 응답")
@Getter
@Builder
@AllArgsConstructor
public class ReportListResponse {

  @Schema(description = "리포트 목록")
  private List<ReportListItem> items;
  @Schema(description = "현재 페이지")
  private int page;
  @Schema(description = "페이지 크기")
  private int size;
  @Schema(description = "전체 건수")
  private long total;
}
