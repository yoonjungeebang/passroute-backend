package passroutebackend.interview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "답변 제출 진행 응답")
@Getter
@AllArgsConstructor
public class AnswerProgressResponse {

  @Schema(description = "꼬리질문 존재 여부")
  private final boolean hasFollowUp;
  @Schema(description = "꼬리질문 ID")
  private final Long followUpQuestionId;
  @Schema(description = "꼬리질문 텍스트")
  private final String followUpQuestionText;
  @Schema(description = "마지막 질문 여부")
  private final boolean lastQuestion;
  @Schema(description = "꼬리질문 TTS 음성 URL")
  private final String audioUrl;

  public static AnswerProgressResponse of(boolean hasFollowUp, Long followUpQuestionId,
      String followUpQuestionText, boolean lastQuestion, String audioUrl) {
    return new AnswerProgressResponse(hasFollowUp, followUpQuestionId, followUpQuestionText,
        lastQuestion, audioUrl);
  }
}
