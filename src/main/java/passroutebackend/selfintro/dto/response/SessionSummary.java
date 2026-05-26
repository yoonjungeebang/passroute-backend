package passroutebackend.selfintro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.interview.dto.report.WeaknessItem;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class SessionSummary {
  private Long sessionId;
  private int round;
  private double score;
  private String strengths;
  private List<WeaknessItem> weaknesses;
  private LocalDateTime date;
}
