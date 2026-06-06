package passroutebackend.debate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import passroutebackend.debate.entity.DebateBranchChoice;

/**
 * POST /debate/{sessionId}/branch 요청. REBUTTAL_1_DECISION에서의 사용자 선택.
 */
@Getter
@NoArgsConstructor
public class DebateBranchRequest {

  @NotNull
  private DebateBranchChoice choice; // rebut_again | finish
}