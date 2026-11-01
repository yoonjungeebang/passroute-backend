package passroutebackend.debate.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import passroutebackend.debate.entity.DebateBranchChoice;

/**
 * POST /debate/{sessionId}/branch 요청. REBUTTAL_1_DECISION에서의 사용자 선택.
 */
@Schema(description = "토론 분기 선택 요청")
@Getter
@NoArgsConstructor
public class DebateBranchRequest {

  @Schema(description = "분기 선택 (rebut_again / finish)")
  @NotNull
  private DebateBranchChoice choice;
}