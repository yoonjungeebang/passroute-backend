package passroutebackend.debate.service;

import org.springframework.stereotype.Service;
import passroutebackend.debate.entity.DebateSession;
import passroutebackend.debate.entity.DebateState;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.entity.Difficulty;

import java.util.Map;

/**
 * 토론 면접의 라운드별 상태 전이를 관리한다.
 *
 * - 메서드는 도메인 객체의 상태만 변경하며, 영속화는 호출자의 @Transactional dirty checking에 맡긴다.
 * - 잘못된 상태 전이는 {@link ErrorCode#INVALID_DEBATE_STATE} 예외로 거부한다.
 * - 낙관적 락(@Version)은 JPA가 자동 처리한다.
 */
@Service
public class DebateStateMachine {

  /** 사용자 턴이 끝난 후 다음으로 갈 AI 턴 상태 */
  private static final Map<DebateState, DebateState> USER_TO_AI = Map.of(
      DebateState.OPENING_USER, DebateState.OPENING_AI,
      DebateState.REBUTTAL_1_USER, DebateState.REBUTTAL_1_AI,
      DebateState.REBUTTAL_2_USER, DebateState.REBUTTAL_2_AI,
      DebateState.CLOSING_USER, DebateState.CLOSING_AI
  );

  // ── 공개 API ────────────────────────────────────────────────────────────────

  /**
   * 사용자 턴 발화 제출 후 호출. *_USER → *_AI 전이.
   */
  public void onUserTurnSubmitted(DebateSession session) {
    requireSession(session);
    DebateState next = USER_TO_AI.get(session.getCurrentState());
    if (next == null) {
      throw CustomException.of(ErrorCode.INVALID_DEBATE_STATE);
    }
    session.transitionTo(next);
  }

  /**
   * AI 경쟁자/면접관 답변 생성 완료 후 호출.
   * *_AI → 다음 *_USER (또는 INTERVIEWER_CLOSING),
   * INTERVIEWER_OPENING → OPENING_USER,
   * INTERVIEWER_CLOSING → FINISHED.
   */
  public void onAiTurnCompleted(DebateSession session) {
    requireSession(session);
    DebateState current = session.getCurrentState();
    DebateState next = switch (current) {
      case INTERVIEWER_OPENING -> DebateState.OPENING_USER;
      case OPENING_AI -> DebateState.REBUTTAL_1_USER;
      case REBUTTAL_1_AI -> isHard(session)
          ? DebateState.REBUTTAL_2_USER
          : DebateState.CLOSING_USER;
      case REBUTTAL_2_AI -> DebateState.CLOSING_USER;
      case CLOSING_AI -> DebateState.INTERVIEWER_CLOSING;
      case INTERVIEWER_CLOSING -> DebateState.FINISHED;
      default -> throw CustomException.of(ErrorCode.INVALID_DEBATE_STATE);
    };
    session.transitionTo(next);
  }

  /**
   * 세션 생성 직후 호출. CREATED → INTERVIEWER_OPENING.
   */
  public void onSessionStarted(DebateSession session) {
    requireSession(session);
    if (session.getCurrentState() != DebateState.CREATED) {
      throw CustomException.of(ErrorCode.INVALID_DEBATE_STATE);
    }
    session.transitionTo(DebateState.INTERVIEWER_OPENING);
  }

  /**
   * 현재 사용자 턴 대기 여부.
   * 컨트롤러의 POST /turn 가드, GET /state 응답 계산용.
   *
   * USER_TO_AI 맵 키셋을 활용하여 새 *_USER 상태 추가 시에도 일관성 자동 유지.
   */
  public boolean isWaitingForUser(DebateSession session) {
    return session != null && USER_TO_AI.containsKey(session.getCurrentState());
  }

  // ── 가드 ──────────────────────────────────────────────────────────────────

  private void requireSession(DebateSession session) {
    if (session == null) {
      throw CustomException.of(ErrorCode.INVALID_INPUT);
    }
  }

  // ── 내부 헬퍼 ──────────────────────────────────────────────────────────────

  private boolean isHard(DebateSession session) {
    return session.getDifficulty() == Difficulty.HARD;
  }
}
