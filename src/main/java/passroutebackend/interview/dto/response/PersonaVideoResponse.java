package passroutebackend.interview.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PersonaVideoResponse {

  private String speakingVideoUrl;
  private String silenceVideoUrl;

  public static PersonaVideoResponse empty() {
    return new PersonaVideoResponse(null, null);
  }
}
