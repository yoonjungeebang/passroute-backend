package passroutebackend.interview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FollowUpResponse {

  @JsonProperty("has_follow_up")
  private final boolean hasFollowUp;

  @JsonProperty("follow_up_question")
  private final String followUpQuestion;

  @JsonProperty("reason")
  private final String reason;

  // 원질문과 같은 목소리로 합성된 꼬리질문 TTS URL (미지원/합성 실패 시 null)
  @JsonProperty("audio_url")
  private final String audioUrl;

  public static FollowUpResponse noFollowUp() {
    return new FollowUpResponse(false, null, null, null);
  }
}
