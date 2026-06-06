package passroutebackend.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnswerSubmitResponse {

  private final boolean hasFollowUp;
  private final Long followUpQuestionId;
  private final String followUpQuestionText;

  // 꼬리질문 TTS 음성 URL (ONE_ON_ONE만 값 존재, 미지원/합성 실패 시 null)
  private final String audioUrl;

  public static AnswerSubmitResponse followUp(Long questionId, String questionText, String audioUrl) {
    return new AnswerSubmitResponse(true, questionId, questionText, audioUrl);
  }

  public static AnswerSubmitResponse noFollowUp() {
    return new AnswerSubmitResponse(false, null, null, null);
  }
}
