package passroutebackend.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "이전 리포트 요약")
@Getter
@AllArgsConstructor
public class PrevReportSummary {

  @Schema(description = "리포트 타입", example = "interview")
  private String reportType;
  @Schema(description = "면접 유형")
  private String interviewType;
  @Schema(description = "면접 일시")
  private LocalDateTime date;
  @Schema(description = "총점")
  private double totalScore;
}
