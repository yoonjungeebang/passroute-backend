package passroutebackend.global.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "persona-video")
public class PersonaVideoProperties {

  private Map<String, VideoUrls> interviewers = new HashMap<>();
  private VideoUrls moderator = new VideoUrls();
  private boolean presignEnabled = true;
  private Duration urlExpiration = Duration.ofHours(6);

  public VideoUrls findInterviewer(String interviewerKey) {
    if (interviewerKey == null) {
      return null;
    }
    return interviewers.get(interviewerKey);
  }

  @Getter
  @Setter
  public static class VideoUrls {
    private String speakingVideoUrl;
    private String silenceVideoUrl;
  }
}
