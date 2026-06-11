package passroutebackend.debate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import passroutebackend.debate.dto.request.DebateSessionCreateRequest;
import passroutebackend.debate.dto.request.DebateTopicGenerateRequest;
import passroutebackend.debate.dto.request.DebateTopicSuggestRequest;
import passroutebackend.debate.dto.request.DebateTurnSubmitRequest;
import passroutebackend.debate.dto.request.SaveDebateWorstClipRequest;
import passroutebackend.debate.dto.response.DebateClipUploadUrlResponse;
import passroutebackend.debate.dto.response.DebateSessionCreateResponse;
import passroutebackend.debate.dto.response.DebateStateResponse;
import passroutebackend.debate.dto.response.DebateTopicCandidateResponse;
import passroutebackend.debate.dto.response.DebateTopicSuggestResponse;
import passroutebackend.debate.dto.response.DebateTurnSummary;
import passroutebackend.debate.dto.response.DebateWorstClipResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import passroutebackend.debate.entity.AiCompetitor;
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
import passroutebackend.debate.entity.TopicCategory;
import passroutebackend.debate.entity.TurnStance;
import passroutebackend.debate.dto.response.DebatePersonaResponse;
import passroutebackend.debate.dto.response.DebateTopicResponse;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.client.AiServerClient;
import passroutebackend.interview.dto.debate.DebateClosingRequest;
import passroutebackend.interview.dto.debate.DebateClosingResponse;
import passroutebackend.interview.dto.debate.DebateOpeningRequest;
import passroutebackend.interview.dto.debate.DebateOpeningResponse;
import passroutebackend.interview.dto.debate.DebateRebuttalRequest;
import passroutebackend.interview.dto.debate.DebateRebuttalResponse;
import passroutebackend.interview.dto.debate.DebateTurnEvalSummary;
import passroutebackend.interview.dto.debate.DebateTurnItem;
import passroutebackend.interview.dto.debate.InterviewerClosingRequest;
import passroutebackend.interview.dto.debate.InterviewerClosingResponse;
import passroutebackend.interview.dto.debate.InterviewerCueRequest;
import passroutebackend.interview.dto.debate.InterviewerCueResponse;
import passroutebackend.interview.dto.debate.InterviewerCueType;
import passroutebackend.interview.dto.debate.InterviewerOpeningRequest;
import passroutebackend.interview.dto.debate.InterviewerOpeningResponse;
import passroutebackend.interview.dto.debate.PersonaPayload;
import passroutebackend.interview.dto.debate.TopicDetailAiRequest;
import passroutebackend.interview.dto.debate.TopicDetailAiResponse;
import passroutebackend.interview.dto.debate.TopicSuggestAiRequest;
import passroutebackend.interview.dto.debate.TopicSuggestAiResponse;
import passroutebackend.selfintro.entity.SelfIntro;

import java.util.List;
import java.util.UUID;

/**
 * 토론 면접 흐름 조율.
 * 상태 머신 호출, 비동기 AI 응답 생성, 사용자 턴 평가 트리거 담당.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DebateService {

  private final DebateTransactionService transactionService;
  private final DebateStateMachine stateMachine;
  private final DebateEvaluationService evaluationService;
  private final AiServerClient aiServerClient;
  private final passroutebackend.interview.service.ClipUploadService clipUploadService;
  /**
   * 자기 자신 프록시. @Async 자가호출은 프록시를 안 거쳐 동기 실행되므로,
   * 컨트롤러 스레드에서 비동기 작업을 띄울 때는 이 프록시를 통해 호출한다.
   * ObjectProvider는 지연 조회라 자기 참조 순환 주입 문제가 없다.
   */
  private final ObjectProvider<DebateService> selfProvider;

  // ── 토픽 / 페르소나 목록 ───────────────────────────────────────────────────

  public List<DebateTopicResponse> listTopics(TopicCategory category) {
    List<DebateTopic> topics = (category == null)
        ? transactionService.findAllTopics()
        : transactionService.findTopicsByCategory(category);
    return topics.stream().map(this::toTopicResponse).toList();
  }

  public List<DebatePersonaResponse> listPersonas() {
    return transactionService.findAllPersonas().stream()
        .map(this::toPersonaResponse)
        .toList();
  }

  private DebateTopicResponse toTopicResponse(DebateTopic topic) {
    return DebateTopicResponse.builder()
        .id(topic.getId())
        .topicKey(topic.getTopicKey())
        .title(topic.getTitle())
        .description(topic.getDescription())
        .category(topic.getCategory())
        .proKeyPoints(parseStringList(topic.getProKeyPoints()))
        .conKeyPoints(parseStringList(topic.getConKeyPoints()))
        .build();
  }

  private DebatePersonaResponse toPersonaResponse(AiPersona persona) {
    return DebatePersonaResponse.builder()
        .id(persona.getId())
        .personaKey(persona.getPersonaKey())
        .name(persona.getName())
        .background(persona.getBackground())
        .debateStyle(persona.getDebateStyle())
        .difficulty(persona.getDifficulty())
        .strengths(parseStringList(persona.getStrengths()))
        .weaknesses(parseStringList(persona.getWeaknesses()))
        .build();
  }

  // ── AI 주제 추천 / 생성 ───────────────────────────────────────────────────

  /** 크롤링 뉴스 기반 주제 후보 N개 추천 (저장하지 않음). */
  public DebateTopicSuggestResponse suggestTopics(Long userId, DebateTopicSuggestRequest req) {
    List<String> keywords = (req.getKeywords() == null)
        ? List.of()
        : req.getKeywords().stream().filter(k -> k != null && !k.isBlank()).toList();
    int count = (req.getCount() == null) ? 3 : req.getCount();
    String companyName = (req.getIntroId() == null)
        ? null
        : transactionService.findSelfIntroOrThrow(req.getIntroId(), userId).getCompanyName();

    TopicSuggestAiResponse ai = aiServerClient.suggestDebateTopics(
        TopicSuggestAiRequest.builder()
            .keywords(keywords)
            .count(count)
            .companyName(companyName)
            .build());

    List<DebateTopicCandidateResponse> candidates = (ai.getCandidates() == null)
        ? List.of()
        : ai.getCandidates().stream()
            .map(c -> DebateTopicCandidateResponse.builder()
                .title(c.getTitle())
                .description(c.getDescription())
                .category(c.getCategory())
                .build())
            .toList();

    return DebateTopicSuggestResponse.builder()
        .candidates(candidates)
        .newsCount(ai.getNewsCount())
        .build();
  }

  /** 선택한 후보를 상세화(찬/반 논거)하여 DB에 저장 → 발급된 topicId(+주제 전체) 반환. */
  public DebateTopicResponse generateTopic(Long userId, DebateTopicGenerateRequest req) {
    String companyName = (req.getIntroId() == null)
        ? null
        : transactionService.findSelfIntroOrThrow(req.getIntroId(), userId).getCompanyName();

    TopicDetailAiResponse ai = aiServerClient.generateDebateTopicDetail(
        TopicDetailAiRequest.builder()
            .title(req.getTitle())
            .summary(req.getDescription())
            .category(req.getCategory())
            .companyName(companyName)
            .build());

    // 찬/반 논거는 토론 진행에 필수 → 누락 시 AI 응답 오류로 처리
    if (ai.getProKeyPoints() == null || ai.getProKeyPoints().isEmpty()
        || ai.getConKeyPoints() == null || ai.getConKeyPoints().isEmpty()) {
      throw CustomException.of(ErrorCode.AI_SERVER_ERROR);
    }

    TopicCategory category = (ai.getCategory() != null) ? ai.getCategory() : req.getCategory();
    String title = (ai.getTopicTitle() != null) ? ai.getTopicTitle() : req.getTitle();
    String description = (ai.getTopicDescription() != null && !ai.getTopicDescription().isBlank())
        ? ai.getTopicDescription()
        : req.getDescription();
    String topicKey = "topic_gen_" + UUID.randomUUID();

    DebateTopic saved = transactionService.saveGeneratedTopic(
        topicKey,
        title,
        description,
        category,
        transactionService.toJson(ai.getProKeyPoints()),
        transactionService.toJson(ai.getConKeyPoints()));

    return toTopicResponse(saved);
  }

  // ── 세션 종료 ─────────────────────────────────────────────────────────────

  public void endSession(Long userId, Long sessionId) {
    DebateSession session = transactionService.findSessionForUserOrThrow(sessionId, userId);
    if (session.getCurrentState() != DebateState.FINISHED) {
      session.transitionTo(DebateState.FINISHED);
      transactionService.saveSession(session);
    }
  }

  // ── 세션 생성 ─────────────────────────────────────────────────────────────

  /** 실전 모드 기본 준비시간(초). 연습 모드는 준비시간 없음. */
  private static final int REAL_PREP_SECONDS = 60;
  private static final int PRACTICE_PREP_SECONDS = 0;

  public DebateSessionCreateResponse createSession(Long userId, DebateSessionCreateRequest req) {
    DebateTopic topic = transactionService.findTopicOrThrow(req.getTopicId());
    AiPersona persona = transactionService.findPersonaOrThrow(req.getPersonaId());
    int prepSeconds = (req.getMode() == DebateMode.REAL)
        ? REAL_PREP_SECONDS
        : PRACTICE_PREP_SECONDS;
    SelfIntro selfIntro = (req.getIntroId() == null)
        ? null
        : transactionService.findSelfIntroOrThrow(req.getIntroId(), userId);
    DebateSession session = transactionService.createSession(
        userId, topic, req.getUserStance(), persona, req.getDifficulty(),
        req.getMode(), prepSeconds,
        selfIntro == null ? null : selfIntro.getId(),
        selfIntro == null ? null : selfIntro.getCompanyName());
    return DebateSessionCreateResponse.builder()
        .sessionId(session.getId())
        .mode(session.getMode())
        .prepSeconds(session.getPrepSeconds())
        .build();
  }

  // ── 세션 시작 (면접관 오프닝 비동기 생성) ──────────────────────────────────

  public void startSession(Long userId, Long sessionId) {
    DebateSession session = transactionService.findSessionForUserOrThrow(sessionId, userId);
    stateMachine.onSessionStarted(session);
    transactionService.saveSession(session);
    selfProvider.getObject().generateInterviewerOpeningAsync(sessionId, userId);
  }

  @Async("debateExecutor")
  public void generateInterviewerOpeningAsync(Long sessionId, Long userId) {
    try {
      DebateSession session = transactionService.findSessionForUserOrThrow(sessionId, userId);
      DebateTopic topic = session.getTopic();
      DebateStance aiStance = session.getAiCompetitor().getStance();

      InterviewerOpeningResponse response = aiServerClient.generateInterviewerOpening(
          InterviewerOpeningRequest.builder()
              .topicTitle(topic.getTitle())
              .topicDescription(topic.getDescription())
              .userStance(session.getUserStance())
              .aiStance(aiStance)
              .difficulty(session.getDifficulty().name())
              .proKeyPoints(parseStringList(topic.getProKeyPoints()))
              .conKeyPoints(parseStringList(topic.getConKeyPoints()))
              .build()
      );

      transactionService.saveTurn(session, SpeakerType.AI_INTERVIEWER, null,
          TurnStance.NEUTRAL, DebateRound.MODERATION, response.getContent(), response.getAudioUrl());
      stateMachine.onAiTurnCompleted(session);
      transactionService.saveSession(session);

    } catch (Exception e) {
      // 오프닝 생성 실패 시 세션이 INTERVIEWER_OPENING에 갇히지 않도록 오프닝을 생략하고 사용자 차례로 진행.
      log.warn("면접관 오프닝 생성 실패, 오프닝 생략 후 사용자 차례로 진행: sessionId={}", sessionId, e);
      try {
        DebateSession session = transactionService.findSessionForUserOrThrow(sessionId, userId);
        if (stateMachine.onInterviewerOpeningFailed(session)) {
          transactionService.saveSession(session);
        }
      } catch (Exception ex) {
        log.warn("오프닝 실패 폴백 처리도 실패: sessionId={}", sessionId, ex);
      }
    }
  }

  // ── 상태 조회 (폴링용) ─────────────────────────────────────────────────────

  public DebateStateResponse getState(Long userId, Long sessionId) {
    DebateSession session = transactionService.findSessionForUserOrThrow(sessionId, userId);
    boolean exposeEval = session.getMode() == DebateMode.PRACTICE;
    List<DebateTurnSummary> latestTurns = transactionService.findTurnsBySession(session).stream()
        .map(turn -> toTurnSummary(turn, exposeEval))
        .toList();
    boolean awaitingDecision = stateMachine.isWaitingForDecision(session);
    return DebateStateResponse.builder()
        .sessionId(session.getId())
        .mode(session.getMode())
        .prepSeconds(session.getPrepSeconds())
        .currentState(session.getCurrentState())
        .isWaitingForUser(stateMachine.isWaitingForUser(session))
        .awaitingDecision(awaitingDecision)
        .availableChoices(awaitingDecision
            ? List.of(DebateBranchChoice.REBUT_AGAIN, DebateBranchChoice.FINISH)
            : List.of())
        .version(session.getVersion())
        .latestTurns(latestTurns)
        .build();
  }

  // ── 사용자 턴 제출 ─────────────────────────────────────────────────────────

  public void submitUserTurn(Long userId, Long sessionId, DebateTurnSubmitRequest req) {
    DebateSession session = transactionService.findSessionForUserOrThrow(sessionId, userId);

    // 가드: 사용자 턴 대기 상태인가?
    if (!stateMachine.isWaitingForUser(session)) {
      throw CustomException.of(ErrorCode.DEBATE_TURN_OUT_OF_ORDER);
    }

    // REAL은 제출 즉시 확정(lock). PRACTICE만 commit 플래그로 시도/확정을 구분한다.
    boolean commit = session.getMode() == DebateMode.REAL || req.isCommit();

    DebateRound round = resolveRound(session.getCurrentState());
    TurnStance userStance = toTurnStance(session.getUserStance());
    String topicTitle = session.getTopic().getTitle();

    // 사용자 발화: FE가 body로 보낸 전사(content)를 우선 사용한다.
    // FE는 AI-WS의 {status:"completed", text}로 전사를 이미 보유하므로, 이를 직접 받으면
    // AI 서버의 pending_stt 비동기 쓰기 타이밍에 의존하지 않아 레이스가 사라진다.
    // content가 없으면(구 경로/타 클라이언트) AI-WS가 채운 pending_stt로 폴백한다.
    String userContent = (req.getContent() != null && !req.getContent().isBlank())
        ? req.getContent()
        : session.getPendingStt();
    boolean hasNewSpeech = userContent != null && !userContent.isBlank();

    // 새 발화가 있으면 저장 + 평가.
    // PRACTICE 재시도는 같은 라운드의 직전 시도를 교체한다 (히스토리/리포트 오염 방지).
    if (hasNewSpeech) {
      // 평가용 상대 직전 발화 = AI 경쟁자의 가장 최근 발화.
      // (재시도로 남아있던 내 직전 시도와 무관하게 조회되도록 경쟁자 발화만 본다)
      List<DebateTurn> competitorTurns = transactionService.findCompetitorTurnsBySession(session);
      String opponentPreviousTurn = competitorTurns.isEmpty()
          ? null
          : competitorTurns.get(competitorTurns.size() - 1).getContent();

      // 직전 시도 삭제 + 새 발화 저장을 단일 트랜잭션으로 묶어 원자성 보장.
      Long turnId = transactionService.replaceUserTurn(session, round, userStance, userContent);
      session.updatePendingStt(null); // 저장은 아래에서 1회로 통합 (낙관적 락 충돌 방지)

      // 턴이 커밋된 뒤 평가 호출 → @Async가 새 턴을 확실히 조회한다.
      // 양 모드 공통 호출·누적 저장, 노출만 GET /state에서 분기 (PRACTICE 즉시 / REAL 종료 리포트).
      evaluationService.evaluateAsync(
          sessionId, userId, turnId,
          userContent, round, session.getUserStance(),
          topicTitle, opponentPreviousTurn);
    }

    // 시도(commit=false, PRACTICE 한정): 라운드를 확정하지 않고 재시도 여지를 둔 채 종료.
    if (!commit) {
      if (!hasNewSpeech) {
        // STT가 아직 pending_stt에 도착하지 않음 → FE에 재시도 신호.
        throw CustomException.of(ErrorCode.DEBATE_STT_NOT_READY);
      }
      transactionService.saveSession(session); // pendingStt 소비만 반영 (상태 전이 없음)
      return;
    }

    // 확정(commit=true): 제출된 발화가 반드시 존재해야 한다.
    // (새 발화도 없고 이전 시도도 없으면 STT 미도착으로 보고 재시도 신호)
    if (!hasNewSpeech && !transactionService.existsUserTurn(session, round)) {
      throw CustomException.of(ErrorCode.DEBATE_STT_NOT_READY);
    }

    // pendingStt 소비 + 라운드 lock(*_USER → *_AI)을 세션 저장 1회로 함께 반영한다.
    // detached 세션을 두 번 저장하면 @Version 낙관적 락 충돌이 나므로 반드시 1회로 통합.
    stateMachine.onUserTurnSubmitted(session);
    transactionService.saveSession(session);

    // AI 경쟁자 답변 생성 (프록시 경유 → 실제 비동기)
    selfProvider.getObject().generateAiCompetitorTurnAsync(sessionId, userId, round);
  }

  // ── 분기 선택 (반박 한 번 더 / 토론 마무리) ──────────────────────────────────

  public void chooseBranch(Long userId, Long sessionId, DebateBranchChoice choice) {
    DebateSession session = transactionService.findSessionForUserOrThrow(sessionId, userId);
    // 가드: REBUTTAL_1_DECISION이 아니면 INVALID_DEBATE_STATE, choice null이면 INVALID_INPUT
    stateMachine.onBranchChosen(session, choice);
    transactionService.saveSession(session);
    // 선택 직후 면접관 cue 생성 (REBUTTAL_EXTRA 또는 CLOSING_GUIDE) → 이후 사용자 턴으로 전이
    // 프록시 경유 → 컨트롤러 스레드를 막지 않고 실제 비동기로 실행
    selfProvider.getObject().generateInterviewerCueAsync(sessionId, userId);
  }

  @Async("debateExecutor")
  public void generateAiCompetitorTurnAsync(Long sessionId, Long userId, DebateRound round) {
    try {
      DebateSession session = transactionService.findSessionForUserOrThrow(sessionId, userId);
      AiCompetitor competitor = session.getAiCompetitor();
      AiPersona persona = competitor.getPersona();
      PersonaPayload personaPayload = toPersonaPayload(persona);

      String content;
      String audioUrl;
      switch (round) {
        case OPENING -> {
          DebateOpeningResponse response = aiServerClient.generateDebateOpening(
              DebateOpeningRequest.builder()
                  .topicTitle(session.getTopic().getTitle())
                  .topicDescription(session.getTopic().getDescription())
                  .stance(competitor.getStance())
                  .difficulty(session.getDifficulty().name())
                  .persona(personaPayload)
                  .proKeyPoints(parseStringList(session.getTopic().getProKeyPoints()))
                  .conKeyPoints(parseStringList(session.getTopic().getConKeyPoints()))
                  .build()
          );
          content = response.getContent();
          audioUrl = response.getAudioUrl();
        }
        case REBUTTAL_1, REBUTTAL_2 -> {
          DebateRebuttalResponse response = aiServerClient.generateDebateRebuttal(
              DebateRebuttalRequest.builder()
                  .topicTitle(session.getTopic().getTitle())
                  .stance(competitor.getStance())
                  .difficulty(session.getDifficulty().name())
                  .persona(personaPayload)
                  .rebuttalRound(round)
                  .opponentLatestTurn(findOpponentPreviousTurn(session))
                  .history(buildHistoryForAi(session))
                  .build()
          );
          content = response.getContent();
          audioUrl = response.getAudioUrl();
        }
        case CLOSING -> {
          DebateClosingResponse response = aiServerClient.generateDebateClosing(
              DebateClosingRequest.builder()
                  .topicTitle(session.getTopic().getTitle())
                  .stance(competitor.getStance())
                  .difficulty(session.getDifficulty().name())
                  .persona(personaPayload)
                  .history(buildHistoryForAi(session))
                  .build()
          );
          content = response.getContent();
          audioUrl = response.getAudioUrl();
        }
        default -> throw CustomException.of(ErrorCode.INVALID_DEBATE_STATE);
      }

      transactionService.saveTurn(session, SpeakerType.AI_COMPETITOR, competitor,
          toTurnStance(competitor.getStance()), round, content, audioUrl);
      stateMachine.onAiTurnCompleted(session);
      transactionService.saveSession(session);

      // AI 경쟁자 발화 후 진입한 상태에 따라 다음 면접관 단계(cue/클로징)를 이어서 트리거.
      // (REBUTTAL_1_DECISION이면 사용자 선택 대기라 아무것도 트리거하지 않음)
      triggerNextInterviewerStep(session, sessionId, userId);

    } catch (Exception e) {
      log.warn("AI 경쟁자 답변 생성 실패: sessionId={}, round={}, error={}",
          sessionId, round, e.getMessage());
    }
  }

  @Async("debateExecutor")
  public void generateInterviewerClosingAsync(Long sessionId, Long userId) {
    try {
      DebateSession session = transactionService.findSessionForUserOrThrow(sessionId, userId);
      InterviewerClosingResponse response = aiServerClient.generateInterviewerClosing(
          InterviewerClosingRequest.builder()
              .topicTitle(session.getTopic().getTitle())
              .history(buildHistoryForAi(session))
              .build()
      );

      transactionService.saveTurn(session, SpeakerType.AI_INTERVIEWER, null,
          TurnStance.NEUTRAL, DebateRound.MODERATION, response.getContent(), response.getAudioUrl());
      stateMachine.onAiTurnCompleted(session);
      transactionService.saveSession(session);

    } catch (Exception e) {
      log.warn("면접관 마무리 생성 실패: sessionId={}, error={}", sessionId, e.getMessage());
    }
  }

  /**
   * 면접관 진행 멘트(cue) 생성. cue 상태(INTERVIEWER_*_CUE)에서 호출.
   * 현재 상태 → cue_type 매핑하여 AI 호출 → AI_INTERVIEWER MODERATION 턴 저장 → 다음 사용자 턴으로 전이.
   */
  @Async("debateExecutor")
  public void generateInterviewerCueAsync(Long sessionId, Long userId) {
    try {
      DebateSession session = transactionService.findSessionForUserOrThrow(sessionId, userId);
      InterviewerCueType cueType = toCueType(session.getCurrentState());

      InterviewerCueResponse response = aiServerClient.generateInterviewerCue(
          InterviewerCueRequest.builder().cueType(cueType).build());

      transactionService.saveTurn(session, SpeakerType.AI_INTERVIEWER, null,
          TurnStance.NEUTRAL, DebateRound.MODERATION, response.getContent(), response.getAudioUrl());
      stateMachine.onAiTurnCompleted(session); // *_CUE → 다음 *_USER
      transactionService.saveSession(session);

    } catch (Exception e) {
      log.warn("면접관 cue 생성 실패: sessionId={}, error={}", sessionId, e.getMessage());
    }
  }

  /**
   * AI 발화/cue 완료 후 진입 상태에 맞는 다음 면접관 단계를 트리거. (사용자 대기 상태면 트리거 없음)
   *
   * <p>호출자({@code generate*Async})가 이미 debateExecutor 백그라운드 스레드에서 돌고 있으므로,
   * 여기서는 프록시를 거치지 않는 직접 호출(동기 연속 실행)이 의도된 동작이다.
   * 라운드 순서 보장이 필요하고 톰캣 스레드도 아니라 별도 비동기 디스패치가 불필요하다.
   */
  private void triggerNextInterviewerStep(DebateSession session, Long sessionId, Long userId) {
    switch (session.getCurrentState()) {
      case INTERVIEWER_REBUTTAL_CUE, INTERVIEWER_REBUTTAL2_CUE, INTERVIEWER_CLOSING_CUE ->
          generateInterviewerCueAsync(sessionId, userId);
      case INTERVIEWER_CLOSING -> generateInterviewerClosingAsync(sessionId, userId);
      default -> { /* REBUTTAL_1_DECISION 등: 사용자 입력/선택 대기 → 트리거 없음 */ }
    }
  }

  private InterviewerCueType toCueType(DebateState state) {
    return switch (state) {
      case INTERVIEWER_REBUTTAL_CUE -> InterviewerCueType.REBUTTAL_START;
      case INTERVIEWER_REBUTTAL2_CUE -> InterviewerCueType.REBUTTAL_EXTRA;
      case INTERVIEWER_CLOSING_CUE -> InterviewerCueType.CLOSING_GUIDE;
      default -> throw CustomException.of(ErrorCode.INVALID_DEBATE_STATE);
    };
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private DebateRound resolveRound(DebateState state) {
    return switch (state) {
      case OPENING_USER -> DebateRound.OPENING;
      case REBUTTAL_1_USER -> DebateRound.REBUTTAL_1;
      case REBUTTAL_2_USER -> DebateRound.REBUTTAL_2;
      case CLOSING_USER -> DebateRound.CLOSING;
      default -> throw CustomException.of(ErrorCode.INVALID_DEBATE_STATE);
    };
  }

  private TurnStance toTurnStance(DebateStance stance) {
    return stance == DebateStance.PRO ? TurnStance.PRO : TurnStance.CON;
  }

  /**
   * @param exposeEval PRACTICE 모드일 때만 true. 사용자 턴 평가 결과/근거를 응답에 노출할지 결정.
   *                   REAL 모드는 false → 평가 필드를 비워 종료 리포트 전까지 감춘다.
   */
  private DebateTurnSummary toTurnSummary(DebateTurn turn, boolean exposeEval) {
    DebateTurnSummary.DebateTurnSummaryBuilder builder = DebateTurnSummary.builder()
        .id(turn.getId())
        .speakerType(turn.getSpeakerType())
        .round(turn.getRound())
        .stance(turn.getStance())
        .content(turn.getContent())
        .audioUrl(turn.getAudioUrl())
        .createdAt(turn.getCreatedAt());

    if (exposeEval && turn.getWeightedScore() != null) {
      builder.weightedScore(turn.getWeightedScore());
      DebateTurnEvalSummary summary = transactionService.parseJson(
          turn.getEvalSummaryJson(), new TypeReference<DebateTurnEvalSummary>() {});
      if (summary != null) {
        builder.evalStrengths(summary.getStrengths());
        builder.evalImprovements(summary.getImprovements());
      }
    }
    return builder.build();
  }

  /** 직전 USER 또는 AI_COMPETITOR 발화 (반박 대상). */
  private String findOpponentPreviousTurn(DebateSession session) {
    List<DebateTurn> turns = transactionService.findTurnsBySession(session);
    List<DebateTurn> filtered = turns.stream()
        .filter(t -> t.getSpeakerType() != SpeakerType.AI_INTERVIEWER)
        .toList();
    return filtered.isEmpty() ? null : filtered.get(filtered.size() - 1).getContent();
  }

  private List<DebateTurnItem> buildHistoryForAi(DebateSession session) {
    return transactionService.findTurnsBySession(session).stream()
        .filter(t -> t.getSpeakerType() != SpeakerType.AI_INTERVIEWER)
        .map(t -> DebateTurnItem.builder()
            .speakerType(t.getSpeakerType())
            .roundType(t.getRound())
            .stance(t.getStance())
            .content(t.getContent())
            .build())
        .toList();
  }

  private PersonaPayload toPersonaPayload(AiPersona persona) {
    return PersonaPayload.builder()
        .personaId(persona.getPersonaKey())
        .name(persona.getName())
        .background(persona.getBackground())
        .debateStyle(persona.getDebateStyle())
        .difficulty(persona.getDifficulty().name())
        .strengths(parseStringList(persona.getStrengths()))
        .weaknesses(parseStringList(persona.getWeaknesses()))
        .systemPromptTemplate(persona.getSystemPromptTemplate())
        .build();
  }

  // ── worst-clip ────────────────────────────────────────────────────────────

  public DebateClipUploadUrlResponse getClipUploadUrl(Long userId, Long sessionId, Long questionId) {
    transactionService.findSessionForUserOrThrow(sessionId, userId);
    var result = clipUploadService.generatePresignedUrl(sessionId, questionId);
    return new DebateClipUploadUrlResponse(result.getUploadUrl(), result.getFileUrl());
  }

  public void saveWorstClip(Long userId, Long sessionId, SaveDebateWorstClipRequest request) {
    transactionService.saveWorstClip(sessionId, userId,
        request.getVideoUrl(), request.getClipScore(), request.getClipReason());
  }

  public DebateWorstClipResponse getWorstClip(Long userId, Long sessionId) {
    return transactionService.getWorstClip(sessionId, userId);
  }

  private List<String> parseStringList(String json) {
    if (json == null) return List.of();
    return transactionService.parseJson(json, new com.fasterxml.jackson.core.type.TypeReference<>() {});
  }
}
