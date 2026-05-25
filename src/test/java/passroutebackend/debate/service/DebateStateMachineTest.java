package passroutebackend.debate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import passroutebackend.debate.entity.DebateSession;
import passroutebackend.debate.entity.DebateStance;
import passroutebackend.debate.entity.DebateState;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.entity.Difficulty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebateStateMachineTest {

  private DebateStateMachine stateMachine;

  @BeforeEach
  void setUp() {
    stateMachine = new DebateStateMachine();
  }

  // ── 1. Easy/Normal 풀 경로 happy path ───────────────────────────────────────

  @Test
  @DisplayName("Normal 난이도 — CREATED → ... → FINISHED 풀 경로 (REBUTTAL_2 없음)")
  void normalDifficultyFullPath() {
    DebateSession session = createSession(Difficulty.NORMAL);

    // CREATED → INTERVIEWER_OPENING
    stateMachine.onSessionStarted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.INTERVIEWER_OPENING);

    // INTERVIEWER_OPENING → OPENING_USER
    stateMachine.onAiTurnCompleted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.OPENING_USER);

    // OPENING_USER → OPENING_AI
    stateMachine.onUserTurnSubmitted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.OPENING_AI);

    // OPENING_AI → REBUTTAL_1_USER
    stateMachine.onAiTurnCompleted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.REBUTTAL_1_USER);

    // REBUTTAL_1_USER → REBUTTAL_1_AI
    stateMachine.onUserTurnSubmitted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.REBUTTAL_1_AI);

    // REBUTTAL_1_AI → CLOSING_USER (Normal이므로 REBUTTAL_2 건너뜀)
    stateMachine.onAiTurnCompleted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.CLOSING_USER);

    // CLOSING_USER → CLOSING_AI
    stateMachine.onUserTurnSubmitted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.CLOSING_AI);

    // CLOSING_AI → INTERVIEWER_CLOSING
    stateMachine.onAiTurnCompleted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.INTERVIEWER_CLOSING);

    // INTERVIEWER_CLOSING → FINISHED
    stateMachine.onAiTurnCompleted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.FINISHED);
    assertThat(session.getEndedAt()).isNotNull();
  }

  // ── 2. Hard 풀 경로 (REBUTTAL_2 포함) ───────────────────────────────────────

  @Test
  @DisplayName("Hard 난이도 — REBUTTAL_1_AI 다음에 REBUTTAL_2 라운드 추가")
  void hardDifficultyIncludesRebuttal2() {
    DebateSession session = createSession(Difficulty.HARD);
    advanceTo(session, DebateState.REBUTTAL_1_AI);

    // REBUTTAL_1_AI → REBUTTAL_2_USER (Hard 분기)
    stateMachine.onAiTurnCompleted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.REBUTTAL_2_USER);

    // REBUTTAL_2_USER → REBUTTAL_2_AI
    stateMachine.onUserTurnSubmitted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.REBUTTAL_2_AI);

    // REBUTTAL_2_AI → CLOSING_USER
    stateMachine.onAiTurnCompleted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.CLOSING_USER);
  }

  // ── 3. 잘못된 상태에서 사용자 턴 제출 → INVALID_DEBATE_STATE ────────────────

  @Test
  @DisplayName("CREATED 상태에서 사용자 턴 제출 시 INVALID_DEBATE_STATE")
  void rejectUserTurnFromCreated() {
    DebateSession session = createSession(Difficulty.NORMAL);

    assertThatThrownBy(() -> stateMachine.onUserTurnSubmitted(session))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_DEBATE_STATE);
  }

  @Test
  @DisplayName("FINISHED 상태에서 사용자 턴 제출 시 INVALID_DEBATE_STATE")
  void rejectUserTurnFromFinished() {
    DebateSession session = createSession(Difficulty.NORMAL);
    session.transitionTo(DebateState.FINISHED);

    assertThatThrownBy(() -> stateMachine.onUserTurnSubmitted(session))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_DEBATE_STATE);
  }

  // ── 4. AI 답변 생성 중 사용자 턴 재호출 거부 ────────────────────────────────

  @Test
  @DisplayName("OPENING_AI 상태(AI 답변 생성 중)에서 사용자 턴 재호출 시 INVALID_DEBATE_STATE")
  void rejectUserTurnDuringAiGeneration() {
    DebateSession session = createSession(Difficulty.NORMAL);
    session.transitionTo(DebateState.OPENING_AI);

    assertThatThrownBy(() -> stateMachine.onUserTurnSubmitted(session))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_DEBATE_STATE);
  }

  // ── 5. null 입력 거부 ──────────────────────────────────────────────────────

  @Test
  @DisplayName("onSessionStarted — session이 null이면 INVALID_INPUT")
  void rejectNullSessionOnStart() {
    assertThatThrownBy(() -> stateMachine.onSessionStarted(null))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
  }

  @Test
  @DisplayName("onUserTurnSubmitted — session이 null이면 INVALID_INPUT")
  void rejectNullSessionOnUserTurn() {
    assertThatThrownBy(() -> stateMachine.onUserTurnSubmitted(null))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
  }

  @Test
  @DisplayName("onAiTurnCompleted — session이 null이면 INVALID_INPUT")
  void rejectNullSessionOnAiTurn() {
    assertThatThrownBy(() -> stateMachine.onAiTurnCompleted(null))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
  }

  @Test
  @DisplayName("isWaitingForUser — session이 null이면 false")
  void isWaitingForUserReturnsFalseForNull() {
    assertThat(stateMachine.isWaitingForUser(null)).isFalse();
  }

  // ── 6. isWaitingForUser 헬퍼 ─────────────────────────────────────────────────

  @Nested
  @DisplayName("isWaitingForUser 헬퍼")
  class IsWaitingForUserTest {

    @Test
    @DisplayName("*_USER 상태에서는 true")
    void trueForUserStates() {
      DebateSession session = createSession(Difficulty.NORMAL);

      session.transitionTo(DebateState.OPENING_USER);
      assertThat(stateMachine.isWaitingForUser(session)).isTrue();

      session.transitionTo(DebateState.REBUTTAL_1_USER);
      assertThat(stateMachine.isWaitingForUser(session)).isTrue();

      session.transitionTo(DebateState.CLOSING_USER);
      assertThat(stateMachine.isWaitingForUser(session)).isTrue();
    }

    @Test
    @DisplayName("*_AI 상태나 면접관 상태, CREATED, FINISHED에서는 false")
    void falseForNonUserStates() {
      DebateSession session = createSession(Difficulty.NORMAL);

      assertThat(stateMachine.isWaitingForUser(session)).isFalse(); // CREATED

      session.transitionTo(DebateState.OPENING_AI);
      assertThat(stateMachine.isWaitingForUser(session)).isFalse();

      session.transitionTo(DebateState.INTERVIEWER_OPENING);
      assertThat(stateMachine.isWaitingForUser(session)).isFalse();

      session.transitionTo(DebateState.FINISHED);
      assertThat(stateMachine.isWaitingForUser(session)).isFalse();
    }
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private DebateSession createSession(Difficulty difficulty) {
    return DebateSession.builder()
        .userId(1L)
        .topic(null) // 단위 테스트라 토픽 인스턴스 불필요
        .userStance(DebateStance.PRO)
        .difficulty(difficulty)
        .build();
  }

  /** 테스트에서 특정 상태까지 이동 */
  private void advanceTo(DebateSession session, DebateState target) {
    session.transitionTo(target);
  }
}
