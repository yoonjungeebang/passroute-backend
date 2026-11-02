package passroutebackend.interview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "답변 제출 응답")
@Getter
@AllArgsConstructor
public class AnswerSubmitResponse {

  @Schema(description = "꼬리질문 존재 여부", example = "true")
  private final boolean hasFollowUp;
  @Schema(description = "꼬리질문 ID", example = "5")
  private final Long followUpQuestionId;
  @Schema(description = "꼬리질문 텍스트", example = "그 경험에서 가장 어려웠던 점은 무엇인가요?")
  private final String followUpQuestionText;
  @Schema(description = "꼬리질문 TTS 음성 URL (1:1 면접만 값 존재, 미지원/합성 실패 시 null)")
  private final String audioUrl;

  public static AnswerSubmitResponse followUp(Long questionId, String questionText, String audioUrl) {
    return new AnswerSubmitResponse(true, questionId, questionText, audioUrl);
  }

  public static AnswerSubmitResponse noFollowUp() {
    return new AnswerSubmitResponse(false, null, null, null);
  }
}
