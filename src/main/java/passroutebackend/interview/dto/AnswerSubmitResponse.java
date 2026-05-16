package passroutebackend.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnswerSubmitResponse {

  private final boolean hasFollowUp;
  private final Long followUpQuestionId;
  private final String followUpQuestionText;

  public static AnswerSubmitResponse followUp(Long questionId, String questionText) {
    return new AnswerSubmitResponse(true, questionId, questionText);
  }

  public static AnswerSubmitResponse noFollowUp() {
    return new AnswerSubmitResponse(false, null, null);
  }
}
