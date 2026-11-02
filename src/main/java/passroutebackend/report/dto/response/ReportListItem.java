package passroutebackend.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "리포트 목록 항목")
@Getter
@AllArgsConstructor
public class ReportListItem {

  @Schema(description = "리포트 타입", example = "interview")
  private String reportType;
  @Schema(description = "세션 ID")
  private Long domainId;
  @Schema(description = "면접 유형")
  private String interviewType;
  @Schema(description = "기업명")
  private String companyName;
  @Schema(description = "직무")
  private String jobPosition;
  @Schema(description = "면접 일시")
  private LocalDateTime date;
  @Schema(description = "총점")
  private double totalScore;
  @Schema(description = "피드백 미리보기")
  private String feedbackPreview;
  @Schema(description = "자소서 ID")
  private Long selfIntroId;
}
