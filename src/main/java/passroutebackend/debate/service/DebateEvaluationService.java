package passroutebackend.debate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import passroutebackend.debate.entity.DebateRound;
import passroutebackend.debate.entity.DebateSession;
import passroutebackend.debate.entity.DebateStance;
import passroutebackend.debate.entity.DebateTurn;
import passroutebackend.debate.entity.SpeakerType;
import passroutebackend.interview.client.AiServerClient;
import passroutebackend.interview.dto.debate.DebateTurnEvalRequest;
import passroutebackend.interview.dto.debate.DebateTurnEvalResponse;
import passroutebackend.interview.dto.debate.DebateTurnItem;

import java.util.List;

/**
 * 사용자 턴 비동기 평가.
 * AI 서버 /evaluate/debate-turn 호출 후 결과를 DebateTurn에 저장.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DebateEvaluationService {

  private final DebateTransactionService transactionService;
  private final AiServerClient aiServerClient;
  private final ObjectMapper objectMapper;

  @Async("debateExecutor")
  public void evaluateAsync(Long sessionId, Long userId, Long turnId,
      String userContent, DebateRound roundType, DebateStance userStance,
      String topicTitle, String opponentPreviousTurn) {
    try {
      DebateSession session = transactionService.findSessionForUserOrThrow(sessionId, userId);
      // 현재 평가 대상 턴은 userContent로 이미 전달되므로 history에서 제외
      List<DebateTurnItem> history = buildHistory(session, turnId);

      DebateTurnEvalResponse response = aiServerClient.evaluateDebateTurn(
          DebateTurnEvalRequest.builder()
              .topicTitle(topicTitle)
              .userStance(userStance)
              .roundType(roundType)
              .userContent(userContent)
              .opponentPreviousTurn(opponentPreviousTurn)
              .history(history)
              .build()
      );

      String scoresJson = toJson(response.getScores());
      String summaryJson = toJson(response.getSummary());

      transactionService.updateTurnEvaluation(
          turnId,
          response.getWeightedScore(),
          scoresJson,
          summaryJson
      );

      log.info("토론 턴 평가 완료: turnId={}, weightedScore={}", turnId, response.getWeightedScore());

    } catch (Exception e) {
      log.warn("토론 턴 평가 실패: turnId={}, error={}", turnId, e.getMessage());
    }
  }

  private List<DebateTurnItem> buildHistory(DebateSession session, Long excludeTurnId) {
    // 사용자/AI 경쟁자 발언만 (면접관 발언 제외, 현재 평가 대상 턴 제외)
    return transactionService.findTurnsBySession(session).stream()
        .filter(t -> t.getSpeakerType() != SpeakerType.AI_INTERVIEWER)
        .filter(t -> !t.getId().equals(excludeTurnId))
        .map(this::toHistoryItem)
        .toList();
  }

  private DebateTurnItem toHistoryItem(DebateTurn turn) {
    return DebateTurnItem.builder()
        .speakerType(turn.getSpeakerType())
        .roundType(turn.getRound())
        .stance(turn.getStance())
        .content(turn.getContent())
        .build();
  }

  private String toJson(Object obj) {
    if (obj == null) return null;
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      log.warn("JSON 직렬화 실패: {}", e.getMessage());
      return null;
    }
  }
}
