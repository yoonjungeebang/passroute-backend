package passroutebackend.interview.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnswerProgressResponse {

  private final boolean hasFollowUp;
  private final Long followUpQuestionId;
  private final String followUpQuestionText;
  private final boolean lastQuestion;

  public static AnswerProgressResponse of(boolean hasFollowUp, Long followUpQuestionId,
      String followUpQuestionText, boolean lastQuestion) {
    return new AnswerProgressResponse(hasFollowUp, followUpQuestionId, followUpQuestionText, lastQuestion);
  }
}
