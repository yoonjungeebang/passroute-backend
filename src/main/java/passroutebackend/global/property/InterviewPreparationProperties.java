package passroutebackend.global.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "interview.preparation")
public class InterviewPreparationProperties {

  private long timeoutMinutes = 3;
  private long cleanupIntervalSeconds = 60;
}