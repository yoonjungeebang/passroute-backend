package passroutebackend.report.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PrevReportSummary {

  private String reportType;        // "interview" | "debate"
  private String interviewType;     // "technical" | "personality" | null
  private LocalDateTime date;
  private double totalScore;
}
