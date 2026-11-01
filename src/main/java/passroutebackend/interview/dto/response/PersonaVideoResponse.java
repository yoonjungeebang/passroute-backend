package passroutebackend.interview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "페르소나 영상 정보")
@Getter
@AllArgsConstructor
public class PersonaVideoResponse {

  @Schema(description = "발화 중 영상 URL")
  private String speakingVideoUrl;
  @Schema(description = "대기 중 영상 URL")
  private String silenceVideoUrl;

  public static PersonaVideoResponse empty() {
    return new PersonaVideoResponse(null, null);
  }
}