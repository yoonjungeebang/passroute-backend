package passroutebackend.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import passroutebackend.interview.dto.response.PersonaVideoResponse;
import passroutebackend.global.property.PersonaVideoProperties;
import passroutebackend.global.property.PersonaVideoProperties.VideoUrls;
import passroutebackend.global.util.PersonaVideoUrlSigner;

@Service
@RequiredArgsConstructor
public class InterviewVideoService {

  private final PersonaVideoProperties properties;
  private final PersonaVideoUrlSigner urlSigner;

  public PersonaVideoResponse getInterviewer(String interviewerKey) {
    return from(properties.findInterviewer(interviewerKey));
  }

  private PersonaVideoResponse from(VideoUrls videoUrls) {
    if (videoUrls == null) {
      return PersonaVideoResponse.empty();
    }
    return new PersonaVideoResponse(
        urlSigner.sign(videoUrls.getSpeakingVideoUrl()),
        urlSigner.sign(videoUrls.getSilenceVideoUrl())
    );
  }
}