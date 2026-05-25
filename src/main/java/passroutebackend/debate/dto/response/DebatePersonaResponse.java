package passroutebackend.debate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.debate.entity.DebateStyle;
import passroutebackend.interview.entity.Difficulty;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DebatePersonaResponse {

  private Long id;
  private String personaKey;
  private String name;
  private String background;
  private DebateStyle debateStyle;
  private Difficulty difficulty;
  private List<String> strengths;
  private List<String> weaknesses;
  // systemPromptTemplate은 응답에 노출 안 함 (LLM 시스템 프롬프트라 클라이언트 불필요)
}
