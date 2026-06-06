package passroutebackend.debate.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DebateTurnSubmitRequest {

  /**
   * 사용자 발화 전사(STT). FE가 AI-WS에서 받은 텍스트를 직접 보내면 이를 우선 사용한다.
   * 비어 있으면 AI-WS가 DB(pending_stt)에 써준 값으로 폴백한다.
   */
  private String content;

  /**
   * 라운드 확정 여부.
   * <p>PRACTICE: false면 시도(평가만, 재시도 가능), true면 확정(라운드 lock + AI 경쟁자 진행).
   * <p>REAL: 무시되며 항상 확정으로 처리된다.
   */
  private boolean commit;
}
