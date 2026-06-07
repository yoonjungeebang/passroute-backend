package passroutebackend.debate.service;

import org.springframework.stereotype.Service;
import passroutebackend.debate.entity.DebateBranchChoice;
import passroutebackend.debate.entity.DebateSession;
import passroutebackend.debate.entity.DebateState;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;

import java.util.Map;

/**
 * 토론 면접의 라운드별 상태 전이를 관리한다.
 *
 * - 메서드는 도메인 객체의 상태만 변경하며, 영속화는 호출자의 @Transactional dirty checking에 맡긴다.
 * - 잘못된 상태 전이는 {@link ErrorCode#INVALID_DEBATE_STATE} 예외로 거부한다.
 * - 낙관적 락(@Version)은 JPA가 자동 처리한다.
 *
 * <p>흐름(난이도 무관 — 반박 2회 여부는 사용자 선택):
 * <pre>
 * INTERVIEWER_OPENING → OPENING_USER → OPENING_AI
 *   → INTERVIEWER_REBUTTAL_CUE → REBUTTAL_1_USER → REBUTTAL_1_AI
 *   → REBUTTAL_1_DECISION ─ REBUT_AGAIN → INTERVIEWER_REBUTTAL2_CUE → REBUTTAL_2_USER → REBUTTAL_2_AI ┐
 *                         └ FINISH ──────────────────────────────────────────────────────────────────┤
 *                                                                              → INTERVIEWER_CLOSING_CUE
 *   → CLOSING_USER → CLOSING_AI → INTERVIEWER_CLOSING → FINISHED
 * </pre>
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
   * 면접관 cue·AI 발화가 끝나면 다음 단계로 전이한다.
   * REBUTTAL_1_AI는 사용자 선택 대기(REBUTTAL_1_DECISION)에서 멈춘다.
   */
  public void onAiTurnCompleted(DebateSession session) {
    requireSession(session);
    DebateState current = session.getCurrentState();
    DebateState next = switch (current) {
      case INTERVIEWER_OPENING -> DebateState.OPENING_USER;
      case OPENING_AI -> DebateState.INTERVIEWER_REBUTTAL_CUE;
      case INTERVIEWER_REBUTTAL_CUE -> DebateState.REBUTTAL_1_USER;
      case REBUTTAL_1_AI -> DebateState.REBUTTAL_1_DECISION;
      case INTERVIEWER_REBUTTAL2_CUE -> DebateState.REBUTTAL_2_USER;
      case REBUTTAL_2_AI -> DebateState.INTERVIEWER_CLOSING_CUE;
      case INTERVIEWER_CLOSING_CUE -> DebateState.CLOSING_USER;
      case CLOSING_AI -> DebateState.INTERVIEWER_CLOSING;
      case INTERVIEWER_CLOSING -> DebateState.FINISHED;
      default -> throw CustomException.of(ErrorCode.INVALID_DEBATE_STATE);
    };
    session.transitionTo(next);
  }

  /**
   * REBUTTAL_1_DECISION에서 사용자 분기 선택 시 호출.
   * REBUT_AGAIN → INTERVIEWER_REBUTTAL2_CUE, FINISH → INTERVIEWER_CLOSING_CUE.
   */
  public void onBranchChosen(DebateSession session, DebateBranchChoice choice) {
    requireSession(session);
    if (choice == null) {
      throw CustomException.of(ErrorCode.INVALID_INPUT);
    }
    if (session.getCurrentState() != DebateState.REBUTTAL_1_DECISION) {
      throw CustomException.of(ErrorCode.INVALID_DEBATE_STATE);
    }
    DebateState next = switch (choice) {
      case REBUT_AGAIN -> DebateState.INTERVIEWER_REBUTTAL2_CUE;
      case FINISH -> DebateState.INTERVIEWER_CLOSING_CUE;
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
   * 면접관 오프닝 생성 실패 시 호출. INTERVIEWER_OPENING → OPENING_USER.
   * 오프닝을 생략하고 사용자 차례로 진행시켜 세션이 INTERVIEWER_OPENING에 갇히지 않게 한다.
   * 이미 다른 상태면(중복 호출/경쟁) 아무것도 하지 않는다(멱등).
   */
  public boolean onInterviewerOpeningFailed(DebateSession session) {
    requireSession(session);
    if (session.getCurrentState() == DebateState.INTERVIEWER_OPENING) {
      session.transitionTo(DebateState.OPENING_USER);
      return true;
    }
    return false;
  }

  /**
   * 현재 사용자 턴(발화) 대기 여부.
   * 컨트롤러의 POST /turn 가드, GET /state 응답 계산용.
   *
   * USER_TO_AI 맵 키셋을 활용하여 새 *_USER 상태 추가 시에도 일관성 자동 유지.
   */
  public boolean isWaitingForUser(DebateSession session) {
    return session != null && USER_TO_AI.containsKey(session.getCurrentState());
  }

  /**
   * 현재 사용자 분기 선택(버튼) 대기 여부.
   * POST /branch 가드, GET /state의 awaitingDecision 계산용.
   */
  public boolean isWaitingForDecision(DebateSession session) {
    return session != null && session.getCurrentState() == DebateState.REBUTTAL_1_DECISION;
  }

  // ── 가드 ──────────────────────────────────────────────────────────────────

  private void requireSession(DebateSession session) {
    if (session == null) {
      throw CustomException.of(ErrorCode.INVALID_INPUT);
    }
  }
}
