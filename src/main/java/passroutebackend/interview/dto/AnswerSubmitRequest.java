package passroutebackend.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnswerSubmitRequest {

  @NotNull
  private final Long questionId;

  @NotBlank
  private final String answerText;
}
