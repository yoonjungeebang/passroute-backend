package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DebateClosingResponse {

  private String content;

  // AI 서버 TTS 음성 URL (null 가능)
  @JsonProperty("audio_url")
  private String audioUrl;
}
