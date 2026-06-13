package passroutebackend.global.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import passroutebackend.global.property.PersonaVideoProperties;
import passroutebackend.global.property.PersonaVideoProperties.VideoUrls;
import passroutebackend.interview.dto.response.PersonaVideoResponse;

@Service
@RequiredArgsConstructor
public class PersonaVideoService {

  private final PersonaVideoProperties properties;

  public PersonaVideoResponse getInterviewer(String interviewerKey) {
    return from(properties.findInterviewer(interviewerKey));
  }

  public PersonaVideoResponse getModerator() {
    return from(properties.getModerator());
  }

  private PersonaVideoResponse from(VideoUrls videoUrls) {
    if (videoUrls == null) {
      return PersonaVideoResponse.empty();
    }
    return new PersonaVideoResponse(
        nullIfBlank(videoUrls.getSpeakingVideoUrl()),
        nullIfBlank(videoUrls.getSilenceVideoUrl())
    );
  }

  private String nullIfBlank(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
