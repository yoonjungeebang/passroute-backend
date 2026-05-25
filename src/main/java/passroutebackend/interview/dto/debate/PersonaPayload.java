package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * AI 서버 토론 호출에 페르소나 전체를 직렬화해 전달하는 페이로드.
 * AI 서버 stateless 유지를 위해 매 호출마다 전체 전달.
 */
@Getter
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PersonaPayload {

  private String personaId;
  private String name;
  private String background;
  private String debateStyle;
  private String difficulty;
  private List<String> strengths;
  private List<String> weaknesses;
  private String systemPromptTemplate;
}
