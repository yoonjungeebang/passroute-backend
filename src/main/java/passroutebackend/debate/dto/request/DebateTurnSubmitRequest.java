package passroutebackend.debate.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "사용자 발화 제출 요청")
@Getter
@NoArgsConstructor
public class DebateTurnSubmitRequest {

  @Schema(description = "사용자 발화 텍스트 (STT)")
  private String content;

  @Schema(description = "라운드 확정 여부 (PRACTICE 모드에서 사용)")
  private boolean commit;
}
