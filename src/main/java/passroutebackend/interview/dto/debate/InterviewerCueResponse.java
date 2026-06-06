package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * POST /debate/interviewer-cue 응답. interviewer-opening/closing과 동일 형태.
 */
@Getter
@Setter
@NoArgsConstructor
public class InterviewerCueResponse {

  private String content;

  // AI 서버 TTS 음성 URL (null 가능)
  @JsonProperty("audio_url")
  private String audioUrl;
}
