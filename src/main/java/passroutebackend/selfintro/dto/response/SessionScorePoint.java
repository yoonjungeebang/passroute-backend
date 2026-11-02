package passroutebackend.selfintro.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "회차별 점수 데이터")
@Getter
@AllArgsConstructor
public class SessionScorePoint {
  @Schema(description = "세션 ID", example = "1")
  private Long sessionId;
  @Schema(description = "회차 번호", example = "3")
  private int round;
  @Schema(description = "세션 점수", example = "82.5")
  private double score;
  @Schema(description = "면접 일시")
  private LocalDateTime date;
}
