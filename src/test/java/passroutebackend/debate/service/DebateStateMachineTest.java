package passroutebackend.debate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import passroutebackend.debate.entity.DebateBranchChoice;
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

  // ── 1. '토론 마무리' 경로 (반박 1회) 풀 happy path ──────────────────────────

  @Test
  @DisplayName("FINISH 분기 — 반박 1회 후 마무리까지 풀 경로 (난이도 무관)")
  void finishBranchFullPath() {
    DebateSession session = createSession();

    stateMachine.onSessionStarted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.INTERVIEWER_OPENING);

    stateMachine.onAiTurnCompleted(session); // 면접관 오프닝 끝
    assertThat(session.getCurrentState()).isEqualTo(DebateState.OPENING_USER);

    stateMachine.onUserTurnSubmitted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.OPENING_AI);

    stateMachine.onAiTurnCompleted(session); // 상대 입론 끝 → 반박 시작 cue
    assertThat(session.getCurrentState()).isEqualTo(DebateState.INTERVIEWER_REBUTTAL_CUE);

    stateMachine.onAiTurnCompleted(session); // 반박 시작 cue 끝
    assertThat(session.getCurrentState()).isEqualTo(DebateState.REBUTTAL_1_USER);

    stateMachine.onUserTurnSubmitted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.REBUTTAL_1_AI);

    stateMachine.onAiTurnCompleted(session); // 상대 반박 끝 → 선택 대기
    assertThat(session.getCurrentState()).isEqualTo(DebateState.REBUTTAL_1_DECISION);

    stateMachine.onBranchChosen(session, DebateBranchChoice.FINISH); // 토론 마무리
    assertThat(session.getCurrentState()).isEqualTo(DebateState.INTERVIEWER_CLOSING_CUE);

    stateMachine.onAiTurnCompleted(session); // 마무리 안내 cue 끝
    assertThat(session.getCurrentState()).isEqualTo(DebateState.CLOSING_USER);

    stateMachine.onUserTurnSubmitted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.CLOSING_AI);

    stateMachine.onAiTurnCompleted(session); // 상대 마무리 끝
    assertThat(session.getCurrentState()).isEqualTo(DebateState.INTERVIEWER_CLOSING);

    stateMachine.onAiTurnCompleted(session); // 면접관 클로징 끝
    assertThat(session.getCurrentState()).isEqualTo(DebateState.FINISHED);
    assertThat(session.getEndedAt()).isNotNull();
  }

  // ── 2. '반박 한 번 더' 경로 (반박 2회) ──────────────────────────────────────

  @Test
  @DisplayName("REBUT_AGAIN 분기 — REBUTTAL_2 진행 후 자동으로 마무리 cue로 (추가 선택 없음)")
  void rebutAgainBranchPath() {
    DebateSession session = createSession();
    advanceTo(session, DebateState.REBUTTAL_1_DECISION);

    stateMachine.onBranchChosen(session, DebateBranchChoice.REBUT_AGAIN);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.INTERVIEWER_REBUTTAL2_CUE);

    stateMachine.onAiTurnCompleted(session); // 추가 반박 안내 cue 끝
    assertThat(session.getCurrentState()).isEqualTo(DebateState.REBUTTAL_2_USER);

    stateMachine.onUserTurnSubmitted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.REBUTTAL_2_AI);

    // 반박2 종료 → 추가 선택 없이 곧장 마무리 cue
    stateMachine.onAiTurnCompleted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.INTERVIEWER_CLOSING_CUE);

    stateMachine.onAiTurnCompleted(session);
    assertThat(session.getCurrentState()).isEqualTo(DebateState.CLOSING_USER);
  }

  // ── 3. onBranchChosen 가드 ──────────────────────────────────────────────────

  @Nested
  @DisplayName("onBranchChosen 가드")
  class OnBranchChosen {

    @Test
    @DisplayName("REBUTTAL_1_DECISION이 아닌 상태에서 분기 선택 시 INVALID_DEBATE_STATE")
    void rejectBranchOutsideDecisionState() {
      DebateSession session = createSession();
      advanceTo(session, DebateState.REBUTTAL_1_AI);

      assertThatThrownBy(() -> stateMachine.onBranchChosen(session, DebateBranchChoice.FINISH))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_DEBATE_STATE);
    }

    @Test
    @DisplayName("choice가 null이면 INVALID_INPUT")
    void rejectNullChoice() {
      DebateSession session = createSession();
      advanceTo(session, DebateState.REBUTTAL_1_DECISION);

      assertThatThrownBy(() -> stateMachine.onBranchChosen(session, null))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("session이 null이면 INVALID_INPUT")
    void rejectNullSession() {
      assertThatThrownBy(() -> stateMachine.onBranchChosen(null, DebateBranchChoice.FINISH))
          .isInstanceOf(CustomException.class)
          .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
    }
  }

  // ── 4. 잘못된 상태에서 사용자 턴 제출 → INVALID_DEBATE_STATE ────────────────

  @Test
  @DisplayName("CREATED 상태에서 사용자 턴 제출 시 INVALID_DEBATE_STATE")
  void rejectUserTurnFromCreated() {
    DebateSession session = createSession();

    assertThatThrownBy(() -> stateMachine.onUserTurnSubmitted(session))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_DEBATE_STATE);
  }

  @Test
  @DisplayName("REBUTTAL_1_DECISION(버튼 대기)에서 사용자 턴 제출 시 INVALID_DEBATE_STATE")
  void rejectUserTurnFromDecision() {
    DebateSession session = createSession();
    advanceTo(session, DebateState.REBUTTAL_1_DECISION);

    assertThatThrownBy(() -> stateMachine.onUserTurnSubmitted(session))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_DEBATE_STATE);
  }

  @Test
  @DisplayName("OPENING_AI 상태(AI 답변 생성 중)에서 사용자 턴 재호출 시 INVALID_DEBATE_STATE")
  void rejectUserTurnDuringAiGeneration() {
    DebateSession session = createSession();
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

  // ── 6. 대기 상태 헬퍼 ────────────────────────────────────────────────────────

  @Nested
  @DisplayName("isWaitingForUser / isWaitingForDecision 헬퍼")
  class WaitingHelpers {

    @Test
    @DisplayName("isWaitingForUser — *_USER 상태에서만 true")
    void waitingForUserTrueOnlyForUserStates() {
      DebateSession session = createSession();

      for (DebateState s : new DebateState[]{
          DebateState.OPENING_USER, DebateState.REBUTTAL_1_USER,
          DebateState.REBUTTAL_2_USER, DebateState.CLOSING_USER}) {
        session.transitionTo(s);
        assertThat(stateMachine.isWaitingForUser(session)).as(s.name()).isTrue();
      }

      for (DebateState s : new DebateState[]{
          DebateState.CREATED, DebateState.OPENING_AI, DebateState.INTERVIEWER_OPENING,
          DebateState.INTERVIEWER_REBUTTAL_CUE, DebateState.REBUTTAL_1_DECISION,
          DebateState.FINISHED}) {
        session.transitionTo(s);
        assertThat(stateMachine.isWaitingForUser(session)).as(s.name()).isFalse();
      }
    }

    @Test
    @DisplayName("isWaitingForDecision — REBUTTAL_1_DECISION에서만 true")
    void waitingForDecisionTrueOnlyForDecisionState() {
      DebateSession session = createSession();

      session.transitionTo(DebateState.REBUTTAL_1_DECISION);
      assertThat(stateMachine.isWaitingForDecision(session)).isTrue();

      session.transitionTo(DebateState.REBUTTAL_1_AI);
      assertThat(stateMachine.isWaitingForDecision(session)).isFalse();

      assertThat(stateMachine.isWaitingForDecision(null)).isFalse();
    }
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private DebateSession createSession() {
    // 난이도는 흐름에 영향 없음(AI 톤 전용). 빌더 요구상 NORMAL 지정.
    return DebateSession.builder()
        .userId(1L)
        .topic(null) // 단위 테스트라 토픽 인스턴스 불필요
        .userStance(DebateStance.PRO)
        .difficulty(Difficulty.NORMAL)
        .build();
  }

  /** 테스트에서 특정 상태까지 이동 */
  private void advanceTo(DebateSession session, DebateState target) {
    session.transitionTo(target);
  }
}
