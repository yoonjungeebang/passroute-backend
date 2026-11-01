package passroutebackend.interview.dto.ai.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SelfIntroItemDto {

  @JsonProperty("question")
  private final String question;

  @JsonProperty("answer")
  private final String answer;
}
