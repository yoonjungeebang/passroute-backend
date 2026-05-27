package passroutebackend.report.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.interview.entity.InterviewReport;
import passroutebackend.interview.entity.InterviewType;
import passroutebackend.interview.repository.InterviewReportRepository;
import passroutebackend.report.dto.response.PrevReportSummary;
import passroutebackend.report.dto.response.UpcomingReportItem;
import passroutebackend.report.dto.response.UpcomingReportResponse;
import passroutebackend.schedule.entity.InterviewSchedule;
import passroutebackend.schedule.repository.InterviewScheduleRepository;

@Service
@RequiredArgsConstructor
public class UpcomingReportService {

  // 미래 일정 조회 시 "먼 미래" 상한 (충분히 큰 값)
  private static final long FUTURE_HORIZON_YEARS = 10;

  private final InterviewScheduleRepository scheduleRepository;
  private final InterviewReportRepository interviewReportRepository;

  @Transactional(readOnly = true)
  public UpcomingReportResponse getUpcoming(Long userId) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime farFuture = now.plusYears(FUTURE_HORIZON_YEARS);

    List<InterviewSchedule> schedules = scheduleRepository
        .findByUserIdAndInterviewDateBetweenOrderByInterviewDateAsc(userId, now, farFuture);

    Map<String, List<InterviewReport>> reportsByCompany = loadPreviousReportsByCompany(userId, schedules);

    List<UpcomingReportItem> items = new ArrayList<>(schedules.size());
    for (InterviewSchedule schedule : schedules) {
      List<InterviewReport> reports = reportsByCompany.getOrDefault(schedule.getCompanyName(), List.of());
      items.add(toItem(schedule, reports));
    }
    return UpcomingReportResponse.builder().items(items).build();
  }

  private Map<String, List<InterviewReport>> loadPreviousReportsByCompany(
      Long userId, List<InterviewSchedule> schedules) {
    List<String> companyNames = schedules.stream()
        .map(InterviewSchedule::getCompanyName)
        .filter(name -> name != null && !name.isBlank())
        .distinct()
        .toList();
    if (companyNames.isEmpty()) return Map.of();

    return interviewReportRepository.findCompletedByUserIdAndCompanyNames(userId, companyNames).stream()
        .collect(Collectors.groupingBy(r -> r.getSession().getInterviewRoom().getCompanyName()));
  }

  private UpcomingReportItem toItem(InterviewSchedule schedule, List<InterviewReport> previousReports) {
    int daysUntil = computeDaysUntil(schedule.getInterviewDate());
    return new UpcomingReportItem(
        schedule.getId(),
        schedule.getCompanyName(),
        schedule.getJobPosition(),
        null,                  // interviewType: schedule에 정보 없음
        schedule.getInterviewDate(),
        daysUntil,
        null,                  // selfIntroId: schedule에 자소서 연결 없음
        null,                  // selfIntroFilename: 동일
        toSummaries(previousReports),
        null                   // aiFeedback: MVP에서 항상 null
    );
  }

  private List<PrevReportSummary> toSummaries(List<InterviewReport> reports) {
    List<PrevReportSummary> summaries = new ArrayList<>(reports.size());
    for (InterviewReport r : reports) {
      InterviewType type = r.getSession().getInterviewRoom().getInterviewType();
      summaries.add(new PrevReportSummary(
          "interview",
          type != null ? type.getValue() : null,   // "technical" | "personality"
          r.getSession().getEndedAt(),
          r.getSessionScore() != null ? r.getSessionScore() : 0.0
      ));
    }
    return summaries;
  }

  private int computeDaysUntil(LocalDateTime scheduledAt) {
    LocalDate today = LocalDate.now();
    LocalDate scheduledDay = scheduledAt.toLocalDate();
    return (int) ChronoUnit.DAYS.between(today, scheduledDay);
  }
}
