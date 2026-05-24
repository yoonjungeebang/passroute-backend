package passroutebackend.interview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import passroutebackend.interview.dto.report.SessionSummaryRequest;
import passroutebackend.interview.dto.report.SessionSummaryResponse;
import passroutebackend.interview.entity.InterviewReadiness;
import passroutebackend.interview.entity.InterviewReport;
import passroutebackend.interview.entity.InterviewType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

  private final AiServerClient aiServerClient;
  private final ReportTransactionService reportTransactionService;
  private final ObjectMapper objectMapper;

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

  @Async("evaluationExecutor")
  public void generateReportAsync(Long sessionId) {
    try {
      if (reportTransactionService.findReport(sessionId).isPresent()) {
        log.info("리포트 이미 존재, 생성 생략 sessionId={}", sessionId);
        return;
      }

      ReportContext ctx = reportTransactionService.loadContext(sessionId);

      if (ctx.questionAnswers().isEmpty()) {
        log.warn("평가 완료된 답변 없음, 리포트 생성 생략 sessionId={}", sessionId);
        return;
      }

      Map<String, ItemStat> stats = computeItemStats(ctx.questionAnswers());
      ItemAverages itemAverages = buildItemAverages(stats);
      SessionScore sessionScore = computeSessionScore(ctx.interviewType(), stats, ctx.questionAnswers());
      InterviewReadiness readiness = determineReadiness(sessionScore.getPercentage(), stats);
      List<String> keyWeakness = computeKeyWeakness(ctx.interviewType(), stats);
      BestWorstQ bestQ = findBestQuestion(ctx.questionAnswers());
      BestWorstQ worstQ = findWorstQuestion(ctx.questionAnswers());

      aiServerClient.sessionSummary(
          new SessionSummaryRequest(
              ctx.jobTitle(),
              ctx.companyName(),
              buildQuestionSummaryItems(ctx.questionAnswers(), ctx.interviewType()),
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
              ctx.jobTitle(),
              ctx.companyName(),
              buildQuestionEvaluations(ctx.questionAnswers(), ctx.interviewType()),
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
          toJson(itemAverages)
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

  public Optional<InterviewReport> findReport(Long sessionId) {
    return reportTransactionService.findReport(sessionId);
  }

  public InterviewReportResponse toResponseDto(InterviewReport report) {
    return toResponse(report);
  }

  // ── 집계 계산 ──────────────────────────────────────────────────────────────

  private record ItemStat(double sum, int count) {
    double average() { return count > 0 ? sum / count : 0.0; }
  }

  private Map<String, ItemStat> computeItemStats(List<QuestionAnswerData> questionAnswers) {
    Map<String, ItemStat> stats = new HashMap<>();
    ITEM_LABELS.keySet().forEach(key -> stats.put(key, new ItemStat(0.0, 0)));

    for (QuestionAnswerData qa : questionAnswers) {
      LlmScores scores = qa.llmScores();
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

      if (qa.concisenessFinal() != null) {
        ItemStat curr = stats.get("conciseness");
        stats.put("conciseness", new ItemStat(curr.sum() + qa.concisenessFinal(), curr.count() + 1));
      } else {
        addStat(stats, "conciseness", scores.getConciseness());
      }
    }
    return stats;
  }

  private void addStat(Map<String, ItemStat> stats, String key, LlmScoreItem item) {
    if (item == null || item.getScore() == null) return;
    ItemStat curr = stats.get(key);
    stats.put(key, new ItemStat(curr.sum() + item.getScore(), curr.count() + 1));
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
    if (stat == null || stat.count() == 0) return null;
    return new ItemAvg(stat.average(), stat.count());
  }

  private SessionScore computeSessionScore(String interviewType, Map<String, ItemStat> stats,
      List<QuestionAnswerData> questionAnswers) {
    boolean isTechnical = InterviewType.TECHNICAL.getValue().equals(interviewType);
    Map<String, Double> weights = isTechnical ? TECHNICAL_WEIGHTS : PERSONALITY_WEIGHTS;

    double weightedSum = 0.0;
    double totalWeight = 0.0;
    for (Map.Entry<String, Double> entry : weights.entrySet()) {
      ItemStat stat = stats.get(entry.getKey());
      if (stat != null && stat.count() > 0) {
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
        .filter(qa -> qa.percentage() != null)
        .map(QuestionAnswerData::percentage)
        .collect(Collectors.toList());

    if (percentages.size() <= 1) return 1.0;

    double max = percentages.stream().mapToDouble(Double::doubleValue).max().orElse(0);
    double min = percentages.stream().mapToDouble(Double::doubleValue).min().orElse(0);
    return Math.max(0.0, 1.0 - ((max - min) / 100.0));
  }

  private InterviewReadiness determineReadiness(double sessionScore, Map<String, ItemStat> stats) {
    OptionalDouble minAvg = stats.values().stream()
        .filter(s -> s.count() > 0)
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
        .filter(e -> e.getValue().count() > 0)
        .collect(Collectors.toList());

    if (valid.isEmpty()) return List.of("뚜렷한 약점 없음");

    double max = valid.stream().mapToDouble(e -> e.getValue().average()).max().orElse(0);
    double min = valid.stream().mapToDouble(e -> e.getValue().average()).min().orElse(0);

    if (max - min < 0.5) return List.of("뚜렷한 약점 없음");

    valid.sort(Comparator
        .comparingDouble((Map.Entry<String, ItemStat> e) -> e.getValue().average())
        .thenComparingInt(e -> -e.getValue().count())
        .thenComparingDouble(e -> -weights.getOrDefault(e.getKey(), 0.0))
    );

    return valid.stream()
        .limit(2)
        .map(e -> ITEM_LABELS.getOrDefault(e.getKey(), e.getKey()))
        .collect(Collectors.toList());
  }

  private BestWorstQ findBestQuestion(List<QuestionAnswerData> questionAnswers) {
    return questionAnswers.stream()
        .filter(qa -> qa.percentage() != null)
        .max(Comparator.comparingDouble(QuestionAnswerData::percentage))
        .map(qa -> new BestWorstQ(qa.questionIndex(), qa.questionText(), qa.percentage(), buildQuestionSummary(qa.llmScores())))
        .orElse(null);
  }

  private BestWorstQ findWorstQuestion(List<QuestionAnswerData> questionAnswers) {
    return questionAnswers.stream()
        .filter(qa -> qa.percentage() != null)
        .min(Comparator.comparingDouble(QuestionAnswerData::percentage))
        .map(qa -> new BestWorstQ(qa.questionIndex(), qa.questionText(), qa.percentage(), buildQuestionSummary(qa.llmScores())))
        .orElse(null);
  }

  // ── DTO 빌더 ───────────────────────────────────────────────────────────────

  private List<QuestionSummaryItem> buildQuestionSummaryItems(List<QuestionAnswerData> questionAnswers, String interviewType) {
    return questionAnswers.stream()
        .map(qa -> new QuestionSummaryItem(
            qa.questionIndex(),
            interviewType,
            qa.questionText(),
            qa.percentage(),
            buildQuestionSummary(qa.llmScores())
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
            qa.questionIndex(),
            interviewType,
            qa.questionText(),
            qa.percentage(),
            buildQuestionSummary(qa.llmScores()),
            new StarEvalForReport(qa.starScore() != null, qa.starScore()),
            null
        ))
        .collect(Collectors.toList());
  }

  private String buildReadinessReason(InterviewReadiness readiness, double percentage, Map<String, ItemStat> stats) {
    return switch (readiness) {
      case READY -> {
        double minAvg = stats.values().stream().filter(s -> s.count() > 0).mapToDouble(ItemStat::average).min().orElse(0);
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
