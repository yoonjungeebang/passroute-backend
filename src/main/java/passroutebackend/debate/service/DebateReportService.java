package passroutebackend.debate.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import passroutebackend.debate.dto.response.DebateReportApiResponse;
import passroutebackend.debate.entity.DebateReport;
import passroutebackend.debate.entity.DebateSession;
import passroutebackend.debate.entity.DebateTurn;
import passroutebackend.interview.client.AiServerClient;
import passroutebackend.interview.dto.debate.DebateReportRequest;
import passroutebackend.interview.dto.debate.DebateReportResponse;
import passroutebackend.interview.dto.debate.DebateSessionSummaryRequest;
import passroutebackend.interview.dto.debate.DebateSessionSummaryResponse;
import passroutebackend.interview.dto.debate.DebateTurnEvalItem;
import passroutebackend.interview.dto.debate.DebateTurnEvalSummary;
import passroutebackend.interview.dto.debate.DebateTurnFeedback;
import passroutebackend.interview.dto.debate.DebateWeaknessItem;
import passroutebackend.interview.entity.ReportStatus;

import java.util.List;
import java.util.Optional;

/**
 * POST /end 시 비동기로 session-summary → report 순차 호출 후 저장.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DebateReportService {

  private final DebateTransactionService transactionService;
  private final AiServerClient aiServerClient;

  @Async("debateExecutor")
  public void generateReportAsync(Long sessionId, Long userId) {
    try {
      DebateSession session = transactionService.findSessionForUserOrThrow(sessionId, userId);

      // 이미 리포트 있으면 중복 생성 방지
      if (transactionService.findReportBySession(session).isPresent()) {
        log.info("리포트 이미 존재, 생성 생략 sessionId={}", sessionId);
        return;
      }

      // 평가 완료된 사용자 턴 추출
      List<DebateTurn> userTurns = transactionService.findTurnsBySession(session).stream()
          .filter(t -> t.getWeightedScore() != null && t.getEvalSummaryJson() != null)
          .toList();

      if (userTurns.isEmpty()) {
        // 평가가 아직 끝나지 않았거나 모두 실패한 상태.
        // 여기서 그냥 return하면 클라이언트가 GET /report에서 무한 202를 받게 됨 → FAILED로 마감.
        log.warn("평가 완료된 사용자 턴 없음, FAILED 리포트 생성 sessionId={}", sessionId);
        transactionService.saveFailedReport(session);
        return;
      }

      List<DebateTurnEvalItem> turnEvaluations = userTurns.stream()
          .map(this::toTurnEvalItem)
          .toList();

      List<String> aiCompetitorTurns = transactionService.findCompetitorTurnsBySession(session)
          .stream()
          .map(DebateTurn::getContent)
          .toList();

      String personaName = session.getAiCompetitor().getPersona().getName();

      // 1. 세션 요약 호출
      DebateSessionSummaryResponse summary = aiServerClient.generateDebateSessionSummary(
          DebateSessionSummaryRequest.builder()
              .topicTitle(session.getTopic().getTitle())
              .userStance(session.getUserStance())
              .difficulty(session.getDifficulty().name())
              .personaName(personaName)
              .turnEvaluations(turnEvaluations)
              .aiCompetitorTurns(aiCompetitorTurns)
              .build()
      );

      // 2. 리포트 생성 호출
      DebateReportResponse report = aiServerClient.generateDebateReport(
          DebateReportRequest.builder()
              .topicTitle(session.getTopic().getTitle())
              .userStance(session.getUserStance())
              .difficulty(session.getDifficulty().name())
              .personaName(personaName)
              .turnEvaluations(turnEvaluations)
              .sessionSummary(summary)
              .build()
      );

      // 3. 세션 점수: 사용자 턴 weighted_score 평균
      double sessionScore = userTurns.stream()
          .mapToDouble(DebateTurn::getWeightedScore)
          .average()
          .orElse(0.0);

      // 4. 저장
      transactionService.saveReport(
          session,
          sessionScore,
          ReportStatus.COMPLETED,
          report.getOverall(),
          report.getStrengths(),
          transactionService.toJson(report.getWeaknesses()),
          report.getImprovements(),
          transactionService.toJson(report.getTurnFeedback()),
          report.getStrategyAnalysis(),
          transactionService.toJson(report.getRecommendedTopics()),
          report.getFinalAdvice(),
          report.getDebateReadinessComment()
      );

      log.info("토론 리포트 생성 완료: sessionId={}, sessionScore={}", sessionId, sessionScore);

    } catch (Exception e) {
      log.warn("토론 리포트 생성 실패: sessionId={}, error={}", sessionId, e.getMessage());
      try {
        DebateSession session = transactionService.findSessionForUserOrThrow(sessionId, userId);
        transactionService.saveFailedReport(session);
      } catch (Exception ex) {
        log.warn("FAILED 상태 저장 실패: sessionId={}", sessionId, ex);
      }
    }
  }

  public Optional<DebateReport> findReport(Long userId, Long sessionId) {
    DebateSession session = transactionService.findSessionForUserOrThrow(sessionId, userId);
    return transactionService.findReportBySession(session);
  }

  public DebateReportApiResponse toApiResponse(DebateReport report) {
    return DebateReportApiResponse.builder()
        .sessionId(report.getSession().getId())
        .sessionScore(report.getSessionScore())
        .overall(report.getOverall())
        .strengths(report.getStrengths())
        .weaknesses(transactionService.parseJson(report.getWeaknesses(), new TypeReference<List<DebateWeaknessItem>>() {}))
        .improvements(report.getImprovements())
        .turnFeedback(transactionService.parseJson(report.getTurnFeedback(), new TypeReference<List<DebateTurnFeedback>>() {}))
        .strategyAnalysis(report.getStrategyAnalysis())
        .recommendedTopics(transactionService.parseJson(report.getRecommendedTopics(), new TypeReference<List<String>>() {}))
        .finalAdvice(report.getFinalAdvice())
        .debateReadinessComment(report.getDebateReadinessComment())
        .createdAt(report.getCreatedAt())
        .build();
  }

  private DebateTurnEvalItem toTurnEvalItem(DebateTurn turn) {
    DebateTurnEvalSummary summary = transactionService.parseJson(
        turn.getEvalSummaryJson(), new TypeReference<DebateTurnEvalSummary>() {});
    return DebateTurnEvalItem.builder()
        .roundType(turn.getRound())
        .userContent(turn.getContent())
        .weightedScore(turn.getWeightedScore())
        .summary(summary)
        .build();
  }
}
