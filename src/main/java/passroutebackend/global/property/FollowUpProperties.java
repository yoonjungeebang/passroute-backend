package passroutebackend.global.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "follow-up")
public class FollowUpProperties {

  private String aiServerUrl;
  private int maxTurn;
  private int timeoutSeconds;
}
