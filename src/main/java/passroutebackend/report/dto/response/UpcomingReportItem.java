package passroutebackend.report.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpcomingReportItem {

  private Long scheduleId;
  private String companyName;
  private String jobPosition;
  private String interviewType;          // 스케줄에 정보 없으므로 항상 null (후속 이슈에서 schedule↔selfintro 연결 시 매핑)
  private LocalDateTime scheduledAt;
  private int daysUntil;                 // 미래 양수, 오늘 0, 지난 음수
  private Long selfIntroId;              // 스케줄에 자소서 연결 없으므로 항상 null (후속 이슈에서 채움)
  private String selfIntroFilename;      // 항상 null
  private List<PrevReportSummary> previousReports;  // companyName 정확 일치로 best-effort 매칭
  private String aiFeedback;             // MVP에서 항상 null
}
