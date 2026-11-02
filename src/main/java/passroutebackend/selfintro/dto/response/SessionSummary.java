package passroutebackend.selfintro.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.report.dto.ai.feedback.WeaknessItem;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "세션 요약 정보")
@Getter
@AllArgsConstructor
public class SessionSummary {
  @Schema(description = "세션 ID", example = "1")
  private Long sessionId;
  @Schema(description = "회차 번호", example = "2")
  private int round;
  @Schema(description = "세션 점수", example = "85.0")
  private double score;
  @Schema(description = "강점 요약")
  private String strengths;
  @Schema(description = "약점 목록")
  private List<WeaknessItem> weaknesses;
  @Schema(description = "면접 일시")
  private LocalDateTime date;
}
