package passroutebackend.report.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportListItem {

  private String reportType;        // "interview" | "debate"
  private Long domainId;            // sessionId
  private String interviewType;     // "technical" | "personality" | null
  private String companyName;       // 토론은 null
  private String jobPosition;       // 토론은 topic.title
  private LocalDateTime date;       // session.endedAt
  private double totalScore;
  private String feedbackPreview;   // overall 앞 50자
  private Long selfIntroId;         // 토론은 null
}
