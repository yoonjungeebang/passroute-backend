package passroutebackend.interview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import passroutebackend.interview.client.AiServerClient;
import passroutebackend.interview.dto.evaluation.LlmScoreItem;
import passroutebackend.interview.dto.evaluation.LlmScores;
import passroutebackend.interview.dto.report.BestWorstQ;
import passroutebackend.interview.dto.report.InterviewReportResponse;
import passroutebackend.interview.dto.report.ItemAvg;
import passroutebackend.interview.dto.report.ItemAverages;
import passroutebackend.interview.dto.report.QuestionSummary;
import passroutebackend.interview.dto.report.QuestionSummaryItem;
import passroutebackend.interview.dto.report.QuestionEvaluationForReport;
import passroutebackend.interview.dto.report.ReadinessForReport;
import passroutebackend.interview.dto.report.ReportGenerationRequest;
import passroutebackend.interview.dto.report.ReportGenerationResponse;
import passroutebackend.interview.dto.report.StarEvalForReport;
import passroutebackend.interview.dto.report.SessionResult;
import passroutebackend.interview.dto.report.QuestionFeedback;
import passroutebackend.interview.dto.report.WeaknessItem;
import passroutebackend.interview.dto.report.SessionScore;
import passroutebackend.interview.dto.report.FaceAnalysisSummary;
import passroutebackend.interview.dto.report.SessionSummaryRequest;
import passroutebackend.interview.dto.report.VoiceAnalysisSummary;
import passroutebackend.interview.entity.FaceAnalysis;
import passroutebackend.interview.entity.InterviewReadiness;
import passroutebackend.interview.entity.InterviewReport;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.entity.InterviewType;
import passroutebackend.interview.entity.ReportStatus;
import passroutebackend.interview.entity.VoiceAnalysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

  private final AiServerClient aiServerClient;
  private final ReportTransactionService reportTransactionService;
  private final InterviewScoreCalculator scoreCalculator;
  private final ObjectMapper objectMapper;

  // 이 시간을 초과해도 GENERATING이면 워커가 죽은 것으로 보고 FAILED 처리(무한 폴링 차단).
  // AI 순차 2콜(최대 240초) + 평가 대기(150초) 여유. 운영에서 report.stale-timeout-seconds로 조정 가능.
  @Value("${report.stale-timeout-seconds:480}")
  private long staleTimeoutSeconds;

  // 리포트 생성 전, 답변별 비동기 평가(percentage)가 끝날 때까지 최대 대기 시간.
  // 종료~평가완료 레이스로 인한 "평가된 답변 0개" 방지. 초과 시 평가된 답변만으로 진행.
  @Value("${report.evaluation-wait-seconds:150}")
  private long evaluationWaitSeconds;

  private static final long EVALUATION_POLL_INTERVAL_MS = 3000L;

  private static final Map<String, Double> TECHNICAL_WEIGHTS = Map.of(
      "relevance", 0.15, "logic", 0.15, "specificity", 0.15,
      "conciseness", 0.10, "clarity", 0.10, "jobRelevance", 0.05,
      "accuracy", 0.15, "depth", 0.15
  );

  private static final Map<String, Double> PERSONALITY_WEIGHTS = Map.of(
      "relevance", 0.15, "logic", 0.20, "specificity", 0.15,
      "conciseness", 0.15, "clarity", 0.15, "jobRelevance", 0.05,
      "authenticity", 0.10, "growth", 0.05
  );

  private static final Map<String, String> ITEM_LABELS;
  static {
    ITEM_LABELS = new LinkedHashMap<>();
    ITEM_LABELS.put("relevance", "질문 적합성");
    ITEM_LABELS.put("logic", "논리성");
    ITEM_LABELS.put("specificity", "구체성");
    ITEM_LABELS.put("conciseness", "간결성");
    ITEM_LABELS.put("clarity", "명확성");
    ITEM_LABELS.put("jobRelevance", "직무 연관성");
    ITEM_LABELS.put("accuracy", "기술 정확성");
    ITEM_LABELS.put("depth", "기술적 깊이");
    ITEM_LABELS.put("authenticity", "진정성");
    ITEM_LABELS.put("growth", "성장 가능성");
  }

  @Async("reportExecutor")
  public void generateReportAsync(Long sessionId) {
    // 시작 시점에 GENERATING row를 즉시 커밋(REQUIRES_NEW). 이미 존재하면 중복 생성 스킵.
    if (!reportTransactionService.startGeneratingReport(sessionId)) {
      log.info("리포트 이미 존재, 생성 생략 sessionId={}", sessionId);
      return;
    }
    try {
      // 답변별 비동기 평가가 끝날 때까지 대기(레이스 방지). 초과 시 평가된 답변만으로 진행.
      awaitEvaluations(sessionId);

      ReportContext ctx = reportTransactionService.loadContext(sessionId);

      if (ctx.getQuestionAnswers().isEmpty()) {
        log.warn("평가 완료된 답변 없음, 리포트 실패 처리 sessionId={}", sessionId);
        reportTransactionService.saveFailedReport(sessionId);
        return;
      }

      Map<String, ItemStat> stats = computeItemStats(ctx.getQuestionAnswers());
      ItemAverages itemAverages = buildItemAverages(stats);
      SessionScore sessionScore = computeSessionScore(ctx.getInterviewType(), stats, ctx.getQuestionAnswers());
      InterviewReadiness readiness = determineReadiness(sessionScore.getPercentage(), stats);
      List<String> keyWeakness = computeKeyWeakness(ctx.getInterviewType(), stats);
      BestWorstQ bestQ = findBestQuestion(ctx.getQuestionAnswers());
      BestWorstQ worstQ = findWorstQuestion(ctx.getQuestionAnswers());

      aiServerClient.sessionSummary(
          new SessionSummaryRequest(
              ctx.getJobTitle(),
              ctx.getCompanyName(),
              buildQuestionSummaryItems(ctx.getQuestionAnswers(), ctx.getInterviewType()),
              itemAverages,
              sessionScore,
              bestQ,
              worstQ
          )
      );

      ReadinessForReport readinessForReport = new ReadinessForReport(
          readiness.name(),
          buildReadinessReason(readiness, sessionScore.getPercentage(), stats)
      );

      ReportGenerationResponse reportResponse = aiServerClient.generateReport(
          new ReportGenerationRequest(
              ctx.getJobTitle(),
              ctx.getCompanyName(),
              buildQuestionEvaluations(ctx.getQuestionAnswers(), ctx.getInterviewType()),
              new SessionResult(
                  sessionScore.getPercentage(),
                  sessionScore.getConsistencyScore(),
                  itemAverages,
                  keyWeakness,
                  readinessForReport
              )
          )
      );

      if (reportResponse == null) {
        log.warn("AI 리포트 응답이 null, sessionId={}", sessionId);
        reportTransactionService.saveFailedReport(sessionId);
        return;
      }

      List<VoiceAnalysis> voiceList = reportTransactionService.loadVoiceAnalysis(sessionId);
      List<FaceAnalysis> faceList = reportTransactionService.loadFaceAnalysis(sessionId);
      InterviewSession session = reportTransactionService.loadSession(sessionId);
      double totalMinutes = scoreCalculator.calcTotalMinutes(session);

      Double voiceScore = null;
      Double avgWpm = null;
      Double avgSilence = null;
      Integer totalFiller = null;
      if (!voiceList.isEmpty()) {
        voiceScore = scoreCalculator.calcVoiceScore(voiceList, totalMinutes);
        avgWpm = voiceList.stream().filter(v -> v.getAvgWpm() != null)
            .mapToDouble(v -> v.getAvgWpm()).average().orElse(0.0);
        avgSilence = voiceList.stream().filter(v -> v.getAvgSilenceDuration() != null)
            .mapToDouble(v -> v.getAvgSilenceDuration()).average().orElse(0.0);
        totalFiller = voiceList.stream().filter(v -> v.getFillerCount() != null)
            .mapToInt(VoiceAnalysis::getFillerCount).sum();
      }

      Double faceScore = null;
      Double avgGazeRatio = null;
      Integer totalGazeOff = null;
      Double avgBlink = null;
      if (!faceList.isEmpty()) {
        faceScore = scoreCalculator.calcFaceScore(faceList, totalMinutes);
        avgGazeRatio = faceList.stream().filter(f -> f.getAvgGazeRatio() != null)
            .mapToDouble(f -> f.getAvgGazeRatio()).average().orElse(0.0);
        totalGazeOff = faceList.stream().filter(f -> f.getGazeOffCount() != null)
            .mapToInt(FaceAnalysis::getGazeOffCount).sum();
        avgBlink = faceList.stream().filter(f -> f.getAvgBlinkPerMin() != null)
            .mapToDouble(f -> f.getAvgBlinkPerMin()).average().orElse(0.0);
      }

      reportTransactionService.saveReport(
          sessionId, sessionScore.getPercentage(), readiness,
          reportResponse.getOverall(),
          reportResponse.getStrengths(),
          toJson(reportResponse.getWeaknesses()),
          reportResponse.getImprovements(),
          toJson(reportResponse.getQuestionFeedback()),
          toJson(reportResponse.getRecommendedQuestions()),
          reportResponse.getFinalAdvice(),
          reportResponse.getReadinessComment(),
          toJson(keyWeakness),
          toJson(itemAverages),
          voiceScore, faceScore,
          avgWpm, avgSilence, totalFiller,
          avgGazeRatio, totalGazeOff, avgBlink
      );

    } catch (Exception e) {
      log.warn("리포트 생성 실패, sessionId={}", sessionId, e);
      try {
        reportTransactionService.saveFailedReport(sessionId);
      } catch (Exception ex) {
        log.warn("FAILED 상태 저장도 실패, sessionId={}", sessionId, ex);
      }
    }
  }

  public Optional<InterviewReport> findReport(Long sessionId, Long userId) {
    return reportTransactionService.findReport(sessionId, userId);
  }

  // 제출된 답변이 0개인지 (전부 스킵/빈답변 → 리포트 생성 불가, 재시도 무의미한 영구 실패 구분용)
  public boolean hasNoAnswers(Long sessionId) {
    return reportTransactionService.countAnswers(sessionId) == 0;
  }

  // 모든 제출 답변의 평가(percentage)가 채워질 때까지 폴링 대기. 초과하면 평가된 것만으로 진행.
  private void awaitEvaluations(Long sessionId) {
    long total = reportTransactionService.countAnswers(sessionId);
    if (total == 0) {
      return; // 제출된 답변 자체가 없음 → 대기 무의미(이후 빈 답변으로 FAILED 처리)
    }
    long deadlineMs = System.currentTimeMillis() + evaluationWaitSeconds * 1000L;
    while (System.currentTimeMillis() < deadlineMs) {
      long evaluated = reportTransactionService.countEvaluatedAnswers(sessionId);
      if (evaluated >= total) {
        return; // 전부 평가 완료
      }
      try {
        Thread.sleep(EVALUATION_POLL_INTERVAL_MS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
    log.warn("평가 완료 대기 {}초 초과, 평가된 답변만으로 리포트 생성 진행 sessionId={}",
        evaluationWaitSeconds, sessionId);
  }

  // GENERATING이 임계값을 초과했으면(워커 사망/배포 중단 등) FAILED로 조건부 전환. 실제 전환되면 true.
  public boolean failIfStale(InterviewReport report) {
    if (report.getReportStatus() != ReportStatus.GENERATING) {
      return false;
    }
    LocalDateTime createdAt = report.getCreatedAt();
    if (createdAt == null
        || createdAt.isAfter(LocalDateTime.now().minusSeconds(staleTimeoutSeconds))) {
      return false; // 임계값 이내 → 정상 진행 중
    }
    boolean flipped = reportTransactionService.markStaleAsFailed(report.getId());
    if (flipped) {
      log.warn("리포트 생성이 임계값({}초) 초과로 FAILED 처리, reportId={}", staleTimeoutSeconds, report.getId());
    }
    return flipped;
  }


  public InterviewReportResponse toResponseDto(InterviewReport report) {
    return toResponse(report);
  }

  // ── 집계 계산 ──────────────────────────────────────────────────────────────

  @Getter
  @AllArgsConstructor
  private static class ItemStat {
    private final double sum;
    private final int count;

    double average() { return count > 0 ? sum / count : 0.0; }
  }

  private Map<String, ItemStat> computeItemStats(List<QuestionAnswerData> questionAnswers) {
    Map<String, ItemStat> stats = new HashMap<>();
    ITEM_LABELS.keySet().forEach(key -> stats.put(key, new ItemStat(0.0, 0)));

    for (QuestionAnswerData qa : questionAnswers) {
      LlmScores scores = qa.getLlmScores();
      if (scores == null) continue;

      addStat(stats, "relevance", scores.getRelevance());
      addStat(stats, "logic", scores.getLogic());
      addStat(stats, "specificity", scores.getSpecificity());
      addStat(stats, "clarity", scores.getClarity());
      addStat(stats, "jobRelevance", scores.getJobRelevance());
      addStat(stats, "accuracy", scores.getAccuracy());
      addStat(stats, "depth", scores.getDepth());
      addStat(stats, "authenticity", scores.getAuthenticity());
      addStat(stats, "growth", scores.getGrowth());

      if (qa.getConcisenessFinal() != null) {
        ItemStat curr = stats.get("conciseness");
        stats.put("conciseness", new ItemStat(curr.getSum() + qa.getConcisenessFinal(), curr.getCount() + 1));
      } else {
        addStat(stats, "conciseness", scores.getConciseness());
      }
    }
    return stats;
  }

  private void addStat(Map<String, ItemStat> stats, String key, LlmScoreItem item) {
    if (item == null || item.getScore() == null) return;
    ItemStat curr = stats.get(key);
    stats.put(key, new ItemStat(curr.getSum() + item.getScore(), curr.getCount() + 1));
  }

  private ItemAverages buildItemAverages(Map<String, ItemStat> stats) {
    return ItemAverages.builder()
        .relevance(toItemAvg(stats, "relevance"))
        .logic(toItemAvg(stats, "logic"))
        .specificity(toItemAvg(stats, "specificity"))
        .conciseness(toItemAvg(stats, "conciseness"))
        .clarity(toItemAvg(stats, "clarity"))
        .jobRelevance(toItemAvg(stats, "jobRelevance"))
        .accuracy(toItemAvg(stats, "accuracy"))
        .depth(toItemAvg(stats, "depth"))
        .authenticity(toItemAvg(stats, "authenticity"))
        .growth(toItemAvg(stats, "growth"))
        .build();
  }

  private ItemAvg toItemAvg(Map<String, ItemStat> stats, String key) {
    ItemStat stat = stats.get(key);
    if (stat == null || stat.getCount() == 0) return null;
    return new ItemAvg(stat.average(), stat.getCount());
  }

  private SessionScore computeSessionScore(String interviewType, Map<String, ItemStat> stats,
      List<QuestionAnswerData> questionAnswers) {
    boolean isTechnical = InterviewType.TECHNICAL.getValue().equals(interviewType);
    Map<String, Double> weights = isTechnical ? TECHNICAL_WEIGHTS : PERSONALITY_WEIGHTS;

    double weightedSum = 0.0;
    double totalWeight = 0.0;
    for (Map.Entry<String, Double> entry : weights.entrySet()) {
      ItemStat stat = stats.get(entry.getKey());
      if (stat != null && stat.getCount() > 0) {
        weightedSum += stat.average() * entry.getValue();
        totalWeight += entry.getValue();
      }
    }

    double raw = totalWeight > 0 ? weightedSum / totalWeight : 0.0;
    double percentage = raw / 5.0 * 100;
    double consistencyScore = computeConsistencyScore(questionAnswers);
    return new SessionScore(raw, percentage, consistencyScore);
  }

  private double computeConsistencyScore(List<QuestionAnswerData> questionAnswers) {
    List<Double> percentages = questionAnswers.stream()
        .filter(qa -> qa.getPercentage() != null)
        .map(QuestionAnswerData::getPercentage)
        .collect(Collectors.toList());

    if (percentages.size() <= 1) return 1.0;

    double max = percentages.stream().mapToDouble(Double::doubleValue).max().orElse(0);
    double min = percentages.stream().mapToDouble(Double::doubleValue).min().orElse(0);
    return Math.max(0.0, 1.0 - ((max - min) / 100.0));
  }

  private InterviewReadiness determineReadiness(double sessionScore, Map<String, ItemStat> stats) {
    OptionalDouble minAvg = stats.values().stream()
        .filter(s -> s.getCount() > 0)
        .mapToDouble(ItemStat::average)
        .min();

    if (sessionScore >= 75 && minAvg.isPresent() && minAvg.getAsDouble() >= 3.0) {
      return InterviewReadiness.READY;
    } else if (sessionScore >= 55) {
      return InterviewReadiness.NEEDS_REVIEW;
    }
    return InterviewReadiness.NEEDS_IMPROVEMENT;
  }

  private List<String> computeKeyWeakness(String interviewType, Map<String, ItemStat> stats) {
    boolean isTechnical = InterviewType.TECHNICAL.getValue().equals(interviewType);
    Map<String, Double> weights = isTechnical ? TECHNICAL_WEIGHTS : PERSONALITY_WEIGHTS;

    List<Map.Entry<String, ItemStat>> valid = stats.entrySet().stream()
        .filter(e -> e.getValue().getCount() > 0)
        .collect(Collectors.toList());

    if (valid.isEmpty()) return List.of("뚜렷한 약점 없음");

    double max = valid.stream().mapToDouble(e -> e.getValue().average()).max().orElse(0);
    double min = valid.stream().mapToDouble(e -> e.getValue().average()).min().orElse(0);

    if (max - min < 0.5) return List.of("뚜렷한 약점 없음");

    valid.sort(Comparator
        .comparingDouble((Map.Entry<String, ItemStat> e) -> e.getValue().average())
        .thenComparingInt(e -> -e.getValue().getCount())
        .thenComparingDouble(e -> -weights.getOrDefault(e.getKey(), 0.0))
    );

    return valid.stream()
        .limit(2)
        .map(e -> ITEM_LABELS.getOrDefault(e.getKey(), e.getKey()))
        .collect(Collectors.toList());
  }

  private BestWorstQ findBestQuestion(List<QuestionAnswerData> questionAnswers) {
    return questionAnswers.stream()
        .filter(qa -> qa.getPercentage() != null)
        .max(Comparator.comparingDouble(QuestionAnswerData::getPercentage))
        .map(qa -> new BestWorstQ(qa.getQuestionIndex(), qa.getQuestionText(), qa.getPercentage(), buildQuestionSummary(qa.getLlmScores())))
        .orElse(null);
  }

  private BestWorstQ findWorstQuestion(List<QuestionAnswerData> questionAnswers) {
    return questionAnswers.stream()
        .filter(qa -> qa.getPercentage() != null)
        .min(Comparator.comparingDouble(QuestionAnswerData::getPercentage))
        .map(qa -> new BestWorstQ(qa.getQuestionIndex(), qa.getQuestionText(), qa.getPercentage(), buildQuestionSummary(qa.getLlmScores())))
        .orElse(null);
  }

  // ── DTO 빌더 ───────────────────────────────────────────────────────────────

  private List<QuestionSummaryItem> buildQuestionSummaryItems(List<QuestionAnswerData> questionAnswers, String interviewType) {
    return questionAnswers.stream()
        .map(qa -> new QuestionSummaryItem(
            qa.getQuestionIndex(),
            interviewType,
            qa.getQuestionText(),
            qa.getPercentage(),
            buildQuestionSummary(qa.getLlmScores())
        ))
        .collect(Collectors.toList());
  }

  private QuestionSummary buildQuestionSummary(LlmScores scores) {
    if (scores == null) return new QuestionSummary("", "");

    List<String> strengths = new ArrayList<>();
    List<String> improvements = new ArrayList<>();

    collectFeedback(scores.getRelevance(), strengths, improvements);
    collectFeedback(scores.getLogic(), strengths, improvements);
    collectFeedback(scores.getSpecificity(), strengths, improvements);
    collectFeedback(scores.getConciseness(), strengths, improvements);
    collectFeedback(scores.getClarity(), strengths, improvements);
    collectFeedback(scores.getAccuracy(), strengths, improvements);
    collectFeedback(scores.getDepth(), strengths, improvements);
    collectFeedback(scores.getAuthenticity(), strengths, improvements);
    collectFeedback(scores.getGrowth(), strengths, improvements);

    return new QuestionSummary(
        String.join(" ", strengths),
        String.join(" ", improvements)
    );
  }

  private void collectFeedback(LlmScoreItem item, List<String> strengths, List<String> improvements) {
    if (item == null || item.getScore() == null || item.getFeedback() == null) return;
    if (item.getScore() >= 4.0) strengths.add(item.getFeedback());
    else if (item.getScore() < 3.0) improvements.add(item.getFeedback());
  }

  private List<QuestionEvaluationForReport> buildQuestionEvaluations(List<QuestionAnswerData> questionAnswers, String interviewType) {
    return questionAnswers.stream()
        .map(qa -> new QuestionEvaluationForReport(
            qa.getQuestionIndex(),
            interviewType,
            qa.getQuestionText(),
            qa.getPercentage(),
            buildQuestionSummary(qa.getLlmScores()),
            new StarEvalForReport(qa.getStarScore() != null, qa.getStarScore()),
            null
        ))
        .collect(Collectors.toList());
  }

  private String buildReadinessReason(InterviewReadiness readiness, double percentage, Map<String, ItemStat> stats) {
    return switch (readiness) {
      case READY -> {
        double minAvg = stats.values().stream().filter(s -> s.getCount() > 0).mapToDouble(ItemStat::average).min().orElse(0);
        yield String.format("세션 점수 %.0f%%, 최저 항목 평균 %.1f 이상으로 기준 충족", percentage, minAvg);
      }
      case NEEDS_REVIEW -> String.format("세션 점수 %.0f%%로 기준 충족하나 일부 항목 보완 필요", percentage);
      case NEEDS_IMPROVEMENT -> String.format("세션 점수 %.0f%%로 전반적인 답변 품질 향상 필요", percentage);
    };
  }

  // ── 응답 변환 ──────────────────────────────────────────────────────────────

  private InterviewReportResponse toResponse(InterviewReport report) {
    return InterviewReportResponse.builder()
        .sessionId(report.getSession().getId())
        .sessionScore(report.getSessionScore())
        .voiceAnalysis(buildVoiceAnalysis(report))
        .faceAnalysis(buildFaceAnalysis(report))
        .interviewReadiness(report.getInterviewReadiness() != null ? report.getInterviewReadiness().name() : null)
        .itemAverages(parseJsonToItemAveragesMap(report.getItemAverages()))
        .keyWeakness(parseJsonAsType(report.getKeyWeakness(), new TypeReference<List<String>>() {}))
        .overall(report.getOverall())
        .strengths(report.getStrengths())
        .weaknesses(parseJsonAsType(report.getWeaknesses(), new TypeReference<List<WeaknessItem>>() {}))
        .improvements(report.getImprovements())
        .questionFeedback(parseJsonAsType(report.getQuestionFeedback(), new TypeReference<List<QuestionFeedback>>() {}))
        .recommendedQuestions(parseJsonAsType(report.getRecommendedQuestions(), new TypeReference<List<String>>() {}))
        .finalAdvice(report.getFinalAdvice())
        .readinessComment(report.getReadinessComment())
        .createdAt(report.getCreatedAt())
        .build();
  }

  private VoiceAnalysisSummary buildVoiceAnalysis(InterviewReport report) {
    if (report.getAvgWpm() == null
        && report.getAvgSilenceDuration() == null
        && report.getFillerCount() == null
        && report.getVoiceScore() == null) {
      return null;
    }
    return new VoiceAnalysisSummary(
        report.getAvgWpm(),
        report.getAvgSilenceDuration(),
        report.getFillerCount(),
        report.getVoiceScore()
    );
  }

  private FaceAnalysisSummary buildFaceAnalysis(InterviewReport report) {
    if (report.getAvgGazeRatio() == null
        && report.getGazeOffCount() == null
        && report.getAvgBlinkPerMin() == null
        && report.getFaceScore() == null) {
      return null;
    }
    return new FaceAnalysisSummary(
        report.getAvgGazeRatio(),
        report.getGazeOffCount(),
        report.getAvgBlinkPerMin(),
        report.getFaceScore()
    );
  }


  // ── JSON 유틸 ──────────────────────────────────────────────────────────────

  private String toJson(Object obj) {
    if (obj == null) return null;
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      log.warn("직렬화 실패: {}", e.getMessage());
      return null;
    }
  }

  private <T> T parseJsonAsType(String json, TypeReference<T> typeReference) {
    if (json == null) return null;
    try {
      return objectMapper.readValue(json, typeReference);
    } catch (Exception e) {
      log.warn("역직렬화 실패: {}", e.getMessage());
      return null;
    }
  }

  private Map<String, Double> parseJsonToItemAveragesMap(String json) {
    if (json == null) return null;
    try {
      ItemAverages averages = objectMapper.readValue(json, ItemAverages.class);
      return averages.toMap();
    } catch (Exception e) {
      return null;
    }
  }
}
