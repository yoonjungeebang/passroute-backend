package passroutebackend.selfintro.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.dto.report.ItemAverages;
import passroutebackend.interview.dto.report.WeaknessItem;
import passroutebackend.interview.entity.InterviewReadiness;
import passroutebackend.interview.entity.InterviewReport;
import passroutebackend.interview.repository.InterviewReportRepository;
import passroutebackend.selfintro.dto.response.ItemTrendItem;
import passroutebackend.selfintro.dto.response.ReadinessInfo;
import passroutebackend.selfintro.dto.response.RecommendedQuestionCount;
import passroutebackend.selfintro.dto.response.SelfIntroReportResponse;
import passroutebackend.selfintro.dto.response.SessionScorePoint;
import passroutebackend.selfintro.dto.response.SessionSummary;
import passroutebackend.selfintro.dto.response.TrendDirection;
import passroutebackend.selfintro.entity.SelfIntro;
import passroutebackend.selfintro.repository.SelfIntroRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SelfIntroReportService {

  private final SelfIntroRepository selfIntroRepository;
  private final InterviewReportRepository interviewReportRepository;
  private final ObjectMapper objectMapper;

  private static final String GROWTH_SUMMARY_N0 = "아직 응시 이력이 없습니다. 첫 면접을 시작해보세요.";
  private static final String GROWTH_SUMMARY_N1 = "1회 응시했습니다. 회차별 추이 분석은 2회차부터 가능합니다.";
  private static final String GROWTH_SUMMARY_UP_TEMPLATE = "%d회차 대비 %d회차에서 %d점 향상되며 성장세를 보이고 있습니다.";
  private static final String GROWTH_SUMMARY_STABLE = "지속적인 응시로 점수가 안정적으로 유지되고 있습니다. 약점 항목 보완에 집중해보세요.";
  private static final String GROWTH_SUMMARY_DOWN = "최근 회차 점수가 하락했습니다. 약점 항목을 점검하고 답변을 다듬어보세요.";

  private static final String READINESS_COMMENT_READY = "합격권 근접 단계입니다. 한두 번 더 연습하면 합격권 진입이 가능합니다.";
  private static final String READINESS_COMMENT_NEEDS_REVIEW = "추가 연습이 필요한 단계입니다. 약점 항목을 중심으로 보완해보세요.";
  private static final String READINESS_COMMENT_NEEDS_IMPROVEMENT = "기초 보강이 필요한 단계입니다. 직무 핵심 기술과 자소서 내용을 다시 점검해보세요.";

  private static final double TREND_THRESHOLD = 0.3;
  private static final double GROWTH_DIFF_THRESHOLD = 3.0;
  private static final double READINESS_READY_CUTOFF = 80.0;
  private static final double READINESS_REVIEW_CUTOFF = 60.0;

  @Transactional(readOnly = true)
  public SelfIntroReportResponse getReport(Long selfIntroId, Long userId) {
    SelfIntro selfIntro = loadOwnedSelfIntro(selfIntroId, userId);

    List<InterviewReport> reports = interviewReportRepository.findAllBySiIdOrderByEndedAtAsc(selfIntroId);

    if (reports.isEmpty()) {
      return buildEmptyResponse(selfIntro);
    }

    return buildResponse(selfIntro, reports);
  }

  private SelfIntroReportResponse buildResponse(SelfIntro selfIntro, List<InterviewReport> reports) {
    List<SessionScorePoint> scoreTimeline = buildScoreTimeline(reports);
    double overallAverage = computeOverallAverage(reports);
    Map<String, Double> itemAverages = computeItemAverages(reports);
    List<ItemTrendItem> itemTrend = buildItemTrend(reports);
    SessionSummary bestSession = findBestSession(reports);
    SessionSummary worstSession = findWorstSession(reports);
    List<RecommendedQuestionCount> topRecommendedQuestions = buildTopRecommendedQuestions(reports);
    ReadinessInfo readiness = buildReadiness(overallAverage);
    String growthSummary = buildGrowthSummary(reports);

    return SelfIntroReportResponse.builder()
        .selfIntroId(selfIntro.getId())
        .companyName(selfIntro.getCompanyName())
        .jobPosition(selfIntro.getJobPosition())
        .totalSessions(reports.size())
        .hasTrendData(reports.size() >= 2)
        .scoreTimeline(scoreTimeline)
        .overallAverage(overallAverage)
        .itemAverages(itemAverages)
        .itemTrend(itemTrend)
        .bestSession(bestSession)
        .worstSession(worstSession)
        .topRecommendedQuestions(topRecommendedQuestions)
        .readiness(readiness)
        .growthSummary(growthSummary)
        .build();
  }

  private List<SessionScorePoint> buildScoreTimeline(List<InterviewReport> reports) {
    List<SessionScorePoint> timeline = new ArrayList<>();
    int round = 1;
    for (InterviewReport r : reports) {
      timeline.add(new SessionScorePoint(
          r.getSession().getId(),
          round++,
          r.getSessionScore(),
          r.getSession().getEndedAt()
      ));
    }
    return timeline;
  }

  private double computeOverallAverage(List<InterviewReport> reports) {
    return reports.stream()
        .mapToDouble(InterviewReport::getSessionScore)
        .average()
        .orElse(0.0);
  }

  private Map<String, Double> computeItemAverages(List<InterviewReport> reports) {
    Map<String, double[]> accum = new LinkedHashMap<>(); // value: [sum, count]
    for (InterviewReport r : reports) {
      Map<String, Double> parsed = parseItemAveragesJson(r.getItemAverages());
      if (parsed == null) continue;
      for (Map.Entry<String, Double> e : parsed.entrySet()) {
        if (e.getValue() == null) continue;
        double[] arr = accum.computeIfAbsent(e.getKey(), k -> new double[]{0.0, 0.0});
        arr[0] += e.getValue();
        arr[1] += 1;
      }
    }
    Map<String, Double> averages = new LinkedHashMap<>();
    for (Map.Entry<String, double[]> e : accum.entrySet()) {
      double[] arr = e.getValue();
      if (arr[1] > 0) {
        averages.put(e.getKey(), arr[0] / arr[1]);
      }
    }
    return averages;
  }

  private Map<String, Double> parseItemAveragesJson(String json) {
    if (json == null || json.isBlank()) return null;
    try {
      ItemAverages averages = objectMapper.readValue(json, ItemAverages.class);
      return averages.toMap();
    } catch (Exception e) {
      log.warn("itemAverages JSON 파싱 실패: {}", e.getMessage());
      return null;
    }
  }

  private SelfIntro loadOwnedSelfIntro(Long selfIntroId, Long userId) {
    SelfIntro selfIntro = selfIntroRepository.findById(selfIntroId)
        .orElseThrow(() -> CustomException.of(ErrorCode.SELF_INTRO_NOT_FOUND));
    if (!selfIntro.isActive()) {
      throw CustomException.of(ErrorCode.SELF_INTRO_NOT_FOUND);
    }
    if (!selfIntro.getUser().getId().equals(userId)) {
      throw CustomException.of(ErrorCode.ACCESS_DENIED);
    }
    return selfIntro;
  }

  private SelfIntroReportResponse buildEmptyResponse(SelfIntro selfIntro) {
    return SelfIntroReportResponse.builder()
        .selfIntroId(selfIntro.getId())
        .companyName(selfIntro.getCompanyName())
        .jobPosition(selfIntro.getJobPosition())
        .totalSessions(0)
        .hasTrendData(false)
        .scoreTimeline(List.of())
        .overallAverage(null)
        .itemAverages(null)
        .itemTrend(List.of())
        .bestSession(null)
        .worstSession(null)
        .topRecommendedQuestions(List.of())
        .readiness(null)
        .growthSummary(GROWTH_SUMMARY_N0)
        .build();
  }

  private List<ItemTrendItem> buildItemTrend(List<InterviewReport> reports) {
    if (reports.size() < 2) return List.of();
    Map<String, Double> first = parseItemAveragesJson(reports.get(0).getItemAverages());
    Map<String, Double> last = parseItemAveragesJson(reports.get(reports.size() - 1).getItemAverages());
    if (first == null || last == null) return List.of();

    List<ItemTrendItem> trends = new ArrayList<>();
    for (Map.Entry<String, Double> e : first.entrySet()) {
      Double firstAvg = e.getValue();
      Double lastAvg = last.get(e.getKey());
      if (firstAvg == null || lastAvg == null) continue;
      double diff = lastAvg - firstAvg;
      trends.add(new ItemTrendItem(e.getKey(), firstAvg, lastAvg, diff, determineDirection(diff)));
    }
    return trends;
  }

  private TrendDirection determineDirection(double diff) {
    if (diff >= TREND_THRESHOLD) return TrendDirection.UP;
    if (diff <= -TREND_THRESHOLD) return TrendDirection.DOWN;
    return TrendDirection.STABLE;
  }

  private SessionSummary findBestSession(List<InterviewReport> reports) {
    int bestIdx = 0;
    for (int i = 1; i < reports.size(); i++) {
      // 동점 시 더 최근(인덱스 큰) 세션으로 갱신
      if (reports.get(i).getSessionScore() >= reports.get(bestIdx).getSessionScore()) {
        bestIdx = i;
      }
    }
    return toSessionSummary(reports.get(bestIdx), bestIdx + 1);
  }

  private SessionSummary findWorstSession(List<InterviewReport> reports) {
    int worstIdx = 0;
    for (int i = 1; i < reports.size(); i++) {
      if (reports.get(i).getSessionScore() <= reports.get(worstIdx).getSessionScore()) {
        worstIdx = i;
      }
    }
    return toSessionSummary(reports.get(worstIdx), worstIdx + 1);
  }

  private SessionSummary toSessionSummary(InterviewReport report, int round) {
    return new SessionSummary(
        report.getSession().getId(),
        round,
        report.getSessionScore(),
        report.getStrengths(),
        parseWeaknessesJson(report.getWeaknesses()),
        report.getSession().getEndedAt()
    );
  }

  private List<WeaknessItem> parseWeaknessesJson(String json) {
    if (json == null || json.isBlank()) return List.of();
    try {
      return objectMapper.readValue(json, new TypeReference<List<WeaknessItem>>() {});
    } catch (Exception e) {
      log.warn("weaknesses JSON 파싱 실패: {}", e.getMessage());
      return List.of();
    }
  }

  private List<RecommendedQuestionCount> buildTopRecommendedQuestions(List<InterviewReport> reports) {
    Map<String, QuestionStat> stat = new LinkedHashMap<>();
    for (InterviewReport r : reports) {
      List<String> questions = parseRecommendedQuestionsJson(r.getRecommendedQuestions());
      if (questions == null) continue;
      LocalDateTime endedAt = r.getSession().getEndedAt();
      for (String q : questions) {
        if (q == null || q.isBlank()) continue;
        QuestionStat s = stat.computeIfAbsent(q, k -> new QuestionStat());
        s.count++;
        if (s.latestEndedAt == null || endedAt.isAfter(s.latestEndedAt)) {
          s.latestEndedAt = endedAt;
        }
      }
    }
    return stat.entrySet().stream()
        .sorted(Comparator
            .comparingInt((Map.Entry<String, QuestionStat> e) -> e.getValue().count).reversed()
            .thenComparing(e -> e.getValue().latestEndedAt, Comparator.reverseOrder()))
        .map(e -> new RecommendedQuestionCount(e.getKey(), e.getValue().count))
        .collect(Collectors.toList());
  }

  private List<String> parseRecommendedQuestionsJson(String json) {
    if (json == null || json.isBlank()) return null;
    try {
      return objectMapper.readValue(json, new TypeReference<List<String>>() {});
    } catch (Exception e) {
      log.warn("recommendedQuestions JSON 파싱 실패: {}", e.getMessage());
      return null;
    }
  }

  private ReadinessInfo buildReadiness(double overallAverage) {
    if (overallAverage >= READINESS_READY_CUTOFF) {
      return new ReadinessInfo(InterviewReadiness.READY, READINESS_COMMENT_READY);
    }
    if (overallAverage >= READINESS_REVIEW_CUTOFF) {
      return new ReadinessInfo(InterviewReadiness.NEEDS_REVIEW, READINESS_COMMENT_NEEDS_REVIEW);
    }
    return new ReadinessInfo(InterviewReadiness.NEEDS_IMPROVEMENT, READINESS_COMMENT_NEEDS_IMPROVEMENT);
  }

  private String buildGrowthSummary(List<InterviewReport> reports) {
    if (reports.size() == 1) {
      return GROWTH_SUMMARY_N1;
    }
    double first = reports.get(0).getSessionScore();
    double last = reports.get(reports.size() - 1).getSessionScore();
    double diff = last - first;
    if (diff >= GROWTH_DIFF_THRESHOLD) {
      return String.format(GROWTH_SUMMARY_UP_TEMPLATE, 1, reports.size(), (int) Math.round(diff));
    }
    if (diff <= -GROWTH_DIFF_THRESHOLD) {
      return GROWTH_SUMMARY_DOWN;
    }
    return GROWTH_SUMMARY_STABLE;
  }

  private static class QuestionStat {
    int count = 0;
    LocalDateTime latestEndedAt;
  }
}
