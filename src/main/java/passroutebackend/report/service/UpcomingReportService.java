package passroutebackend.report.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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

    List<UpcomingReportItem> items = new ArrayList<>(schedules.size());
    for (InterviewSchedule schedule : schedules) {
      items.add(toItem(schedule, userId));
    }
    return UpcomingReportResponse.builder().items(items).build();
  }

  private UpcomingReportItem toItem(InterviewSchedule schedule, Long userId) {
    List<PrevReportSummary> previous = findPreviousReports(userId, schedule.getCompanyName());
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
        previous,
        null                   // aiFeedback: MVP에서 항상 null
    );
  }

  private List<PrevReportSummary> findPreviousReports(Long userId, String companyName) {
    if (companyName == null || companyName.isBlank()) {
      return List.of();
    }
    List<InterviewReport> reports =
        interviewReportRepository.findCompletedByUserIdAndCompanyName(userId, companyName);
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
