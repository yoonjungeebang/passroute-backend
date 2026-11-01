package passroutebackend.global.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "interview.preparation")
public class InterviewPreparationProperties {

  private int maxConcurrent = 3;
  private long timeoutMinutes = 3;
  private long cleanupIntervalSeconds = 60;
}