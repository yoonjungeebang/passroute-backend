package passroutebackend.interview.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.debate.entity.AiPersona;

@Getter
@AllArgsConstructor
public class PersonaVideoResponse {

  private String speakingVideoUrl;
  private String silenceVideoUrl;

  public static PersonaVideoResponse from(AiPersona persona) {
    if (persona == null) {
      return empty();
    }
    return new PersonaVideoResponse(persona.getSpeakingVideoUrl(), persona.getSilenceVideoUrl());
  }

  public static PersonaVideoResponse empty() {
    return new PersonaVideoResponse(null, null);
  }
}
