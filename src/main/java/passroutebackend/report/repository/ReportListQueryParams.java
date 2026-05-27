package passroutebackend.report.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportListQueryParams {

  private final Long userId;
  private final boolean includeInterview;
  private final boolean includeDebate;
  private final String interviewTypeFilter;   // "TECHNICAL" | "PERSONALITY" | null
  private final Long resumeId;                // siId, 없으면 null
  private final String q;                     // "%lowered%" 형태, 없으면 null
}
