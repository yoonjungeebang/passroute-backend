package passroutebackend.debate.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import passroutebackend.debate.dto.request.DebateSessionCreateRequest;
import passroutebackend.debate.dto.request.DebateTurnSubmitRequest;
import passroutebackend.debate.dto.response.DebateSessionCreateResponse;
import passroutebackend.debate.dto.response.DebateStateResponse;
import passroutebackend.debate.entity.AiPersona;
import passroutebackend.debate.entity.DebateMode;
import passroutebackend.debate.entity.DebateRound;
import passroutebackend.debate.entity.DebateSession;
import passroutebackend.debate.entity.DebateStance;
import passroutebackend.debate.entity.DebateState;
import passroutebackend.debate.entity.DebateTopic;
import passroutebackend.debate.entity.DebateTurn;
import passroutebackend.debate.entity.SpeakerType;
import passroutebackend.debate.entity.TurnStance;
import passroutebackend.interview.client.AiServerClient;
import passroutebackend.interview.dto.debate.DebateTurnEvalSummary;
import passroutebackend.interview.entity.Difficulty;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 연습(PRACTICE)/실전(REAL) 모드 분기 로직 단위 테스트.
 *
 * <p>핵심 분기 3가지를 검증한다.
 * <ul>
 *   <li>준비시간: REAL=60초, PRACTICE=0초</li>
 *   <li>재시도/lock: PRACTICE commit=false는 평가만 하고 라운드 유지, commit=true/REAL은 즉시 lock + AI 진행</li>
 *   <li>평가 노출: PRACTICE만 /state에 평가 결과 노출, REAL은 마스킹</li>
 * </ul>
 *
 * <p>AI 경쟁자 비동기 생성은 {@code @Spy}로 차단하여 분기 호출 여부만 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class DebateModeServiceTest {

  private static final Long USER_ID = 1L;
  private static final Long SESSION_ID = 100L;

  @Mock private DebateTransactionService transactionService;
  @Mock private DebateStateMachine stateMachine;
  @Mock private DebateEvaluationService evaluationService;
  @Mock private AiServerClient aiServerClient;

  @InjectMocks @Spy private DebateService debateService;

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private DebateSession sessionWithMode(DebateMode mode) {
    DebateTopic topic = DebateTopic.builder()
        .topicKey("k").title("AI 윤리").build();
    DebateSession session = DebateSession.builder()
        .userId(USER_ID)
        .topic(topic)
        .userStance(DebateStance.PRO)
        .difficulty(Difficulty.NORMAL)
        .mode(mode)
        .prepSeconds(mode == DebateMode.REAL ? 60 : 0)
        .build();
    session.transitionTo(DebateState.OPENING_USER); // 사용자 턴 대기 상태
    return session;
  }

  private void stubForSubmit(DebateSession session) {
    when(transactionService.findSessionForUserOrThrow(SESSION_ID, USER_ID)).thenReturn(session);
    when(stateMachine.isWaitingForUser(session)).thenReturn(true);
    when(transactionService.findCompetitorTurnsBySession(session)).thenReturn(List.of());
    when(transactionService.replaceUserTurn(eq(session), eq(DebateRound.OPENING), any(), any()))
        .thenReturn(5L);
  }

  private DebateTurnSubmitRequest submitRequest(boolean commit) {
    DebateTurnSubmitRequest req = new DebateTurnSubmitRequest();
    ReflectionTestUtils.setField(req, "commit", commit);
    return req;
  }

  // ── 세션 생성: 준비시간 분기 ───────────────────────────────────────────────

  @Nested
  @DisplayName("createSession - 모드별 준비시간")
  class CreateSession {

    private DebateSessionCreateRequest createRequest(DebateMode mode) {
      DebateSessionCreateRequest req = new DebateSessionCreateRequest();
      ReflectionTestUtils.setField(req, "topicId", 10L);
      ReflectionTestUtils.setField(req, "userStance", DebateStance.PRO);
      ReflectionTestUtils.setField(req, "personaId", 20L);
      ReflectionTestUtils.setField(req, "difficulty", Difficulty.NORMAL);
      ReflectionTestUtils.setField(req, "mode", mode);
      return req;
    }

    @Test
    @DisplayName("REAL 모드는 준비시간 60초로 저장한다")
    void realModeGets60Seconds() {
      DebateTopic topic = DebateTopic.builder().topicKey("k").title("AI 윤리").build();
      AiPersona persona = org.mockito.Mockito.mock(AiPersona.class);
      when(transactionService.findTopicOrThrow(10L)).thenReturn(topic);
      when(transactionService.findPersonaOrThrow(20L)).thenReturn(persona);
      DebateSession saved = sessionWithMode(DebateMode.REAL);
      ReflectionTestUtils.setField(saved, "id", SESSION_ID);
      when(transactionService.createSession(
          eq(USER_ID), eq(topic), eq(DebateStance.PRO), eq(persona),
          eq(Difficulty.NORMAL), eq(DebateMode.REAL), eq(60))).thenReturn(saved);

      DebateSessionCreateResponse res =
          debateService.createSession(USER_ID, createRequest(DebateMode.REAL));

      assertThat(res.getMode()).isEqualTo(DebateMode.REAL);
      assertThat(res.getPrepSeconds()).isEqualTo(60);
      verify(transactionService).createSession(
          eq(USER_ID), eq(topic), eq(DebateStance.PRO), eq(persona),
          eq(Difficulty.NORMAL), eq(DebateMode.REAL), eq(60));
    }

    @Test
    @DisplayName("PRACTICE 모드는 준비시간 0초로 저장한다")
    void practiceModeGets0Seconds() {
      DebateTopic topic = DebateTopic.builder().topicKey("k").title("AI 윤리").build();
      AiPersona persona = org.mockito.Mockito.mock(AiPersona.class);
      when(transactionService.findTopicOrThrow(10L)).thenReturn(topic);
      when(transactionService.findPersonaOrThrow(20L)).thenReturn(persona);
      DebateSession saved = sessionWithMode(DebateMode.PRACTICE);
      ReflectionTestUtils.setField(saved, "id", SESSION_ID);
      when(transactionService.createSession(
          eq(USER_ID), eq(topic), eq(DebateStance.PRO), eq(persona),
          eq(Difficulty.NORMAL), eq(DebateMode.PRACTICE), eq(0))).thenReturn(saved);

      DebateSessionCreateResponse res =
          debateService.createSession(USER_ID, createRequest(DebateMode.PRACTICE));

      assertThat(res.getMode()).isEqualTo(DebateMode.PRACTICE);
      assertThat(res.getPrepSeconds()).isEqualTo(0);
      verify(transactionService).createSession(
          eq(USER_ID), eq(topic), eq(DebateStance.PRO), eq(persona),
          eq(Difficulty.NORMAL), eq(DebateMode.PRACTICE), eq(0));
    }
  }

  // ── 사용자 턴 제출: 재시도/lock 분기 ───────────────────────────────────────

  @Nested
  @DisplayName("submitUserTurn - 모드별 재시도/lock")
  class SubmitUserTurn {

    @Test
    @DisplayName("PRACTICE commit=false: 평가만 하고 라운드를 lock하지 않으며 AI 경쟁자를 진행하지 않는다")
    void practiceAttemptDoesNotLock() {
      DebateSession session = sessionWithMode(DebateMode.PRACTICE);
      session.updatePendingStt("내 발화");
      stubForSubmit(session);

      debateService.submitUserTurn(USER_ID, SESSION_ID, submitRequest(false));

      // 평가는 시도에서도 호출된다
      verify(evaluationService).evaluateAsync(
          any(), any(), any(), any(), any(), any(), any(), any());
      // 라운드 미확정: lock/AI 진행 없음
      verify(stateMachine, never()).onUserTurnSubmitted(any());
      verify(debateService, never()).generateAiCompetitorTurnAsync(anyLong(), anyLong(), any());
      // 직전 시도 교체(삭제+저장 원자적)는 호출된다
      verify(transactionService).replaceUserTurn(eq(session), eq(DebateRound.OPENING), any(), any());
      // pendingStt 소비는 세션 저장 1회로 반영된다
      verify(transactionService).saveSession(session);
      assertThat(session.getPendingStt()).isNull();
    }

    @Test
    @DisplayName("PRACTICE commit=true: 라운드를 lock하고 AI 경쟁자를 진행한다")
    void practiceCommitLocksAndAdvances() {
      DebateSession session = sessionWithMode(DebateMode.PRACTICE);
      session.updatePendingStt("내 발화");
      stubForSubmit(session);
      doNothing().when(debateService).generateAiCompetitorTurnAsync(anyLong(), anyLong(), any());

      debateService.submitUserTurn(USER_ID, SESSION_ID, submitRequest(true));

      verify(evaluationService).evaluateAsync(
          any(), any(), any(), any(), any(), any(), any(), any());
      verify(stateMachine).onUserTurnSubmitted(session);
      verify(debateService).generateAiCompetitorTurnAsync(SESSION_ID, USER_ID, DebateRound.OPENING);
    }

    @Test
    @DisplayName("REAL: commit=false 요청이라도 제출 즉시 lock하고 AI 경쟁자를 진행한다")
    void realModeAlwaysLocksOnSubmit() {
      DebateSession session = sessionWithMode(DebateMode.REAL);
      session.updatePendingStt("내 발화");
      stubForSubmit(session);
      doNothing().when(debateService).generateAiCompetitorTurnAsync(anyLong(), anyLong(), any());

      debateService.submitUserTurn(USER_ID, SESSION_ID, submitRequest(false));

      verify(stateMachine).onUserTurnSubmitted(session);
      verify(debateService).generateAiCompetitorTurnAsync(SESSION_ID, USER_ID, DebateRound.OPENING);
    }
  }

  // ── 상태 조회: 평가 노출 분기 ───────────────────────────────────────────────

  @Nested
  @DisplayName("getState - 모드별 평가 노출")
  class GetStateEvalExposure {

    private DebateTurn evaluatedUserTurn(DebateSession session) {
      DebateTurn turn = DebateTurn.builder()
          .session(session).speakerType(SpeakerType.USER)
          .stance(TurnStance.PRO).round(DebateRound.OPENING).content("내 발화").build();
      turn.updateEvaluation(8.5, "{\"logic\":1}", "{\"strengths\":\"좋음\"}");
      return turn;
    }

    @Test
    @DisplayName("PRACTICE: 평가 점수와 강점/개선점을 노출한다")
    void practiceExposesEval() {
      DebateSession session = sessionWithMode(DebateMode.PRACTICE);
      DebateTurn turn = evaluatedUserTurn(session);
      when(transactionService.findSessionForUserOrThrow(SESSION_ID, USER_ID)).thenReturn(session);
      when(transactionService.findTurnsBySession(session)).thenReturn(List.of(turn));
      DebateTurnEvalSummary summary = new DebateTurnEvalSummary();
      summary.setStrengths("좋음");
      summary.setImprovements("더 구체적으로");
      when(transactionService.parseJson(any(), any())).thenReturn(summary);

      DebateStateResponse res = debateService.getState(USER_ID, SESSION_ID);

      assertThat(res.getMode()).isEqualTo(DebateMode.PRACTICE);
      assertThat(res.getLatestTurns()).hasSize(1);
      assertThat(res.getLatestTurns().get(0).getWeightedScore()).isEqualTo(8.5);
      assertThat(res.getLatestTurns().get(0).getEvalStrengths()).isEqualTo("좋음");
      assertThat(res.getLatestTurns().get(0).getEvalImprovements()).isEqualTo("더 구체적으로");
    }

    @Test
    @DisplayName("REAL: 평가 결과를 마스킹하여 노출하지 않는다")
    void realMasksEval() {
      DebateSession session = sessionWithMode(DebateMode.REAL);
      DebateTurn turn = evaluatedUserTurn(session);
      when(transactionService.findSessionForUserOrThrow(SESSION_ID, USER_ID)).thenReturn(session);
      when(transactionService.findTurnsBySession(session)).thenReturn(List.of(turn));

      DebateStateResponse res = debateService.getState(USER_ID, SESSION_ID);

      assertThat(res.getMode()).isEqualTo(DebateMode.REAL);
      assertThat(res.getPrepSeconds()).isEqualTo(60);
      assertThat(res.getLatestTurns()).hasSize(1);
      assertThat(res.getLatestTurns().get(0).getWeightedScore()).isNull();
      assertThat(res.getLatestTurns().get(0).getEvalStrengths()).isNull();
      assertThat(res.getLatestTurns().get(0).getEvalImprovements()).isNull();
      // 마스킹 시에는 평가 JSON 파싱조차 하지 않는다
      verify(transactionService, never()).parseJson(any(), any());
    }
  }
}
