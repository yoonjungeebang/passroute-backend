package passroutebackend.selfintro.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.report.entity.InterviewReadiness;

@Schema(description = "면접 준비도 정보")
@Getter
@AllArgsConstructor
public class ReadinessInfo {
  @Schema(description = "준비도 등급")
  private InterviewReadiness level;
  @Schema(description = "준비도 코멘트", example = "전반적으로 준비가 잘 되어 있습니다.")
  private String comment;
}
