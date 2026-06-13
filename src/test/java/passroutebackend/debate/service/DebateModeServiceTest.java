package passroutebackend.debate.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;
import passroutebackend.debate.dto.request.DebateSessionCreateRequest;
import passroutebackend.debate.dto.request.DebateTurnSubmitRequest;
import passroutebackend.debate.dto.response.DebateSessionCreateResponse;
import passroutebackend.debate.dto.response.DebateStateResponse;
import passroutebackend.debate.entity.AiPersona;
import passroutebackend.debate.entity.DebateBranchChoice;
import passroutebackend.debate.entity.DebateMode;
import passroutebackend.debate.entity.DebateRound;
import passroutebackend.debate.entity.DebateSession;
import passroutebackend.debate.entity.DebateStance;
import passroutebackend.debate.entity.DebateState;
import passroutebackend.debate.entity.DebateTopic;
import passroutebackend.debate.entity.DebateTurn;
import passroutebackend.debate.entity.SpeakerType;
import passroutebackend.debate.entity.TurnStance;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.service.PersonaVideoService;
import passroutebackend.interview.client.AiServerClient;
import passroutebackend.interview.dto.debate.DebateTurnEvalSummary;
import passroutebackend.interview.dto.response.PersonaVideoResponse;
import passroutebackend.interview.entity.Difficulty;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
  @Mock private ObjectProvider<DebateService> selfProvider;
  @Mock private PersonaVideoService personaVideoService;

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
      when(persona.getSpeakingVideoUrl()).thenReturn("https://s3/opponent-speaking.mp4");
      when(persona.getSilenceVideoUrl()).thenReturn("https://s3/opponent-silence.mp4");
      when(transactionService.findTopicOrThrow(10L)).thenReturn(topic);
      when(transactionService.findPersonaOrThrow(20L)).thenReturn(persona);
      when(personaVideoService.getModerator())
          .thenReturn(new PersonaVideoResponse("https://s3/moderator-speaking.mp4",
              "https://s3/moderator-silence.mp4"));
      DebateSession saved = sessionWithMode(DebateMode.REAL);
      ReflectionTestUtils.setField(saved, "id", SESSION_ID);
      when(transactionService.createSession(
          eq(USER_ID), eq(topic), eq(DebateStance.PRO), eq(persona),
          eq(Difficulty.NORMAL), eq(DebateMode.REAL), eq(60),
          isNull(), isNull())).thenReturn(saved);

      DebateSessionCreateResponse res =
          debateService.createSession(USER_ID, createRequest(DebateMode.REAL));

      assertThat(res.getMode()).isEqualTo(DebateMode.REAL);
      assertThat(res.getPrepSeconds()).isEqualTo(60);
      assertThat(res.getModerator().getSpeakingVideoUrl())
          .isEqualTo("https://s3/moderator-speaking.mp4");
      assertThat(res.getOpponent().getSilenceVideoUrl())
          .isEqualTo("https://s3/opponent-silence.mp4");
      verify(transactionService).createSession(
          eq(USER_ID), eq(topic), eq(DebateStance.PRO), eq(persona),
          eq(Difficulty.NORMAL), eq(DebateMode.REAL), eq(60),
          isNull(), isNull());
    }

    @Test
    @DisplayName("PRACTICE 모드는 준비시간 0초로 저장한다")
    void practiceModeGets0Seconds() {
      DebateTopic topic = DebateTopic.builder().topicKey("k").title("AI 윤리").build();
      AiPersona persona = org.mockito.Mockito.mock(AiPersona.class);
      when(transactionService.findTopicOrThrow(10L)).thenReturn(topic);
      when(transactionService.findPersonaOrThrow(20L)).thenReturn(persona);
      when(personaVideoService.getModerator()).thenReturn(PersonaVideoResponse.empty());
      DebateSession saved = sessionWithMode(DebateMode.PRACTICE);
      ReflectionTestUtils.setField(saved, "id", SESSION_ID);
      when(transactionService.createSession(
          eq(USER_ID), eq(topic), eq(DebateStance.PRO), eq(persona),
          eq(Difficulty.NORMAL), eq(DebateMode.PRACTICE), eq(0),
          isNull(), isNull())).thenReturn(saved);

      DebateSessionCreateResponse res =
          debateService.createSession(USER_ID, createRequest(DebateMode.PRACTICE));

      assertThat(res.getMode()).isEqualTo(DebateMode.PRACTICE);
      assertThat(res.getPrepSeconds()).isEqualTo(0);
      verify(transactionService).createSession(
          eq(USER_ID), eq(topic), eq(DebateStance.PRO), eq(persona),
          eq(Difficulty.NORMAL), eq(DebateMode.PRACTICE), eq(0),
          isNull(), isNull());
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
      when(selfProvider.getObject()).thenReturn(debateService);
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
      when(selfProvider.getObject()).thenReturn(debateService);
      doNothing().when(debateService).generateAiCompetitorTurnAsync(anyLong(), anyLong(), any());

      debateService.submitUserTurn(USER_ID, SESSION_ID, submitRequest(false));

      verify(stateMachine).onUserTurnSubmitted(session);
      verify(debateService).generateAiCompetitorTurnAsync(SESSION_ID, USER_ID, DebateRound.OPENING);
    }

    @Test
    @DisplayName("body content가 있으면 pending_stt보다 우선 사용한다 (레이스 제거)")
    void prefersBodyContentOverPendingStt() {
      DebateSession session = sessionWithMode(DebateMode.PRACTICE);
      session.updatePendingStt("AI가 DB에 늦게 써준 값");
      stubForSubmit(session);
      when(selfProvider.getObject()).thenReturn(debateService);
      doNothing().when(debateService).generateAiCompetitorTurnAsync(anyLong(), anyLong(), any());
      DebateTurnSubmitRequest req = submitRequest(true);
      ReflectionTestUtils.setField(req, "content", "FE가 보낸 전사");

      debateService.submitUserTurn(USER_ID, SESSION_ID, req);

      verify(transactionService).replaceUserTurn(
          eq(session), eq(DebateRound.OPENING), any(), eq("FE가 보낸 전사"));
      verify(evaluationService).evaluateAsync(
          any(), any(), any(), eq("FE가 보낸 전사"), any(), any(), any(), any());
    }

    @Test
    @DisplayName("content도 pending_stt도 없으면(STT 미도착) STT_NOT_READY로 거부하고 아무것도 저장하지 않는다")
    void rejectsWhenSttNotReady() {
      DebateSession session = sessionWithMode(DebateMode.PRACTICE); // pendingStt 미설정(null)
      when(transactionService.findSessionForUserOrThrow(SESSION_ID, USER_ID)).thenReturn(session);
      when(stateMachine.isWaitingForUser(session)).thenReturn(true);

      assertThatThrownBy(() ->
          debateService.submitUserTurn(USER_ID, SESSION_ID, submitRequest(false)))
          .isInstanceOf(CustomException.class);

      verify(transactionService, never()).replaceUserTurn(any(), any(), any(), any());
      verify(evaluationService, never()).evaluateAsync(
          any(), any(), any(), any(), any(), any(), any(), any());
    }
  }

  // ── 분기 선택 wiring ────────────────────────────────────────────────────────

  @Nested
  @DisplayName("chooseBranch - 분기 선택 위임/트리거")
  class ChooseBranch {

    @Test
    @DisplayName("상태머신에 선택 위임 후 세션 저장 + 면접관 cue 생성을 트리거한다")
    void delegatesAndTriggersCue() {
      DebateSession session = sessionWithMode(DebateMode.PRACTICE);
      when(transactionService.findSessionForUserOrThrow(SESSION_ID, USER_ID)).thenReturn(session);
      when(selfProvider.getObject()).thenReturn(debateService);
      doNothing().when(debateService).generateInterviewerCueAsync(anyLong(), anyLong());

      debateService.chooseBranch(USER_ID, SESSION_ID, DebateBranchChoice.FINISH);

      verify(stateMachine).onBranchChosen(session, DebateBranchChoice.FINISH);
      verify(transactionService).saveSession(session);
      verify(debateService).generateInterviewerCueAsync(SESSION_ID, USER_ID);
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
      attachPersona(session);
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
      assertThat(res.getModerator()).isNotNull();
      assertThat(res.getOpponent().getSpeakingVideoUrl())
          .isEqualTo("https://s3/opponent-speaking.mp4");
    }

    @Test
    @DisplayName("REAL: 평가 결과를 마스킹하여 노출하지 않는다")
    void realMasksEval() {
      DebateSession session = sessionWithMode(DebateMode.REAL);
      attachPersona(session);
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

    private void attachPersona(DebateSession session) {
      AiPersona persona = AiPersona.builder()
          .personaKey("test-persona")
          .name("테스트 상대")
          .debateStyle(passroutebackend.debate.entity.DebateStyle.COOPERATIVE)
          .difficulty(Difficulty.NORMAL)
          .speakingVideoUrl("https://s3/opponent-speaking.mp4")
          .silenceVideoUrl("https://s3/opponent-silence.mp4")
          .build();
      passroutebackend.debate.entity.AiCompetitor competitor =
          passroutebackend.debate.entity.AiCompetitor.builder()
              .session(session)
              .persona(persona)
              .stance(DebateStance.CON)
              .build();
      ReflectionTestUtils.setField(session, "aiCompetitor", competitor);
      when(personaVideoService.getModerator()).thenReturn(PersonaVideoResponse.empty());
    }
  }
}
