package passroutebackend.selfintro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SessionScorePoint {
  private Long sessionId;
  private int round;
  private double score;
  private LocalDateTime date;
}
