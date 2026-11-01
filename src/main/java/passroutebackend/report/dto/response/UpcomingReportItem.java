package passroutebackend.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "다가오는 면접 항목")
@Getter
@AllArgsConstructor
public class UpcomingReportItem {

  @Schema(description = "일정 ID")
  private Long scheduleId;
  @Schema(description = "기업명")
  private String companyName;
  @Schema(description = "직무")
  private String jobPosition;
  @Schema(description = "면접 유형")
  private String interviewType;
  @Schema(description = "예정 일시")
  private LocalDateTime scheduledAt;
  @Schema(description = "D-day (미래 양수, 오늘 0, 과거 음수)")
  private int daysUntil;
  @Schema(description = "자소서 ID")
  private Long selfIntroId;
  @Schema(description = "자소서 파일명")
  private String selfIntroFilename;
  @Schema(description = "이전 리포트 요약 목록")
  private List<PrevReportSummary> previousReports;
  @Schema(description = "AI 피드백")
  private String aiFeedback;
}
