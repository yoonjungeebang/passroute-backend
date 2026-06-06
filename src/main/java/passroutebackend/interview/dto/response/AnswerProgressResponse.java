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

  // 꼬리질문 TTS 음성 URL (ONE_ON_ONE만 값 존재, 미지원/합성 실패 시 null)
  private final String audioUrl;

  public static AnswerProgressResponse of(boolean hasFollowUp, Long followUpQuestionId,
      String followUpQuestionText, boolean lastQuestion, String audioUrl) {
    return new AnswerProgressResponse(hasFollowUp, followUpQuestionId, followUpQuestionText,
        lastQuestion, audioUrl);
  }
}
