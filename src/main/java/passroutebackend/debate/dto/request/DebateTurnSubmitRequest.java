package passroutebackend.debate.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DebateTurnSubmitRequest {

  private String content;

  /**
   * 라운드 확정 여부.
   * <p>PRACTICE: false면 시도(평가만, 재시도 가능), true면 확정(라운드 lock + AI 경쟁자 진행).
   * <p>REAL: 무시되며 항상 확정으로 처리된다.
   */
  private boolean commit;
}
