package passroutebackend.debate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import passroutebackend.debate.entity.AiPersona;
import passroutebackend.debate.dto.response.PersonaVideoResponse;
import passroutebackend.global.property.PersonaVideoProperties;
import passroutebackend.global.property.PersonaVideoProperties.VideoUrls;
import passroutebackend.global.util.PersonaVideoUrlSigner;

@Service
@RequiredArgsConstructor
public class DebateVideoService {

  private final PersonaVideoProperties properties;
  private final PersonaVideoUrlSigner urlSigner;

  public PersonaVideoResponse getModerator() {
    return from(properties.getModerator());
  }

  public PersonaVideoResponse getPersona(AiPersona persona) {
    if (persona == null) {
      return PersonaVideoResponse.empty();
    }
    return from(persona.getSpeakingVideoUrl(), persona.getSilenceVideoUrl());
  }

  private PersonaVideoResponse from(VideoUrls videoUrls) {
    if (videoUrls == null) {
      return PersonaVideoResponse.empty();
    }
    return from(videoUrls.getSpeakingVideoUrl(), videoUrls.getSilenceVideoUrl());
  }

  private PersonaVideoResponse from(String speakingVideoUrl, String silenceVideoUrl) {
    return new PersonaVideoResponse(
        urlSigner.sign(speakingVideoUrl),
        urlSigner.sign(silenceVideoUrl)
    );
  }
}