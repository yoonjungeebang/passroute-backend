package passroutebackend.interview.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "꼬리질문 생성 응답 (AI 서버)")
@Getter
@AllArgsConstructor
public class FollowUpResponse {

  @Schema(description = "꼬리질문 존재 여부", example = "true")
  @JsonProperty("has_follow_up")
  private final boolean hasFollowUp;

  @Schema(description = "꼬리질문 텍스트", example = "그 기술을 선택한 이유가 무엇인가요?")
  @JsonProperty("follow_up_question")
  private final String followUpQuestion;

  @Schema(description = "꼬리질문 생성 사유", example = "답변이 구체적이지 않아 추가 질문이 필요합니다.")
  @JsonProperty("reason")
  private final String reason;

  @Schema(description = "꼬리질문 TTS 음성 URL (미지원/합성 실패 시 null)")
  @JsonProperty("audio_url")
  private final String audioUrl;

  public static FollowUpResponse noFollowUp() {
    return new FollowUpResponse(false, null, null, null);
  }
}
