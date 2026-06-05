package passroutebackend.debate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import passroutebackend.debate.dto.request.DebateSessionCreateRequest;
import passroutebackend.debate.dto.request.DebateTopicGenerateRequest;
import passroutebackend.debate.dto.request.DebateTopicSuggestRequest;
import passroutebackend.debate.dto.request.DebateTurnSubmitRequest;
import passroutebackend.debate.dto.response.DebateSessionCreateResponse;
import passroutebackend.debate.dto.response.DebateStateResponse;
import passroutebackend.debate.dto.response.DebateTopicCandidateResponse;
import passroutebackend.debate.dto.response.DebateTopicSuggestResponse;
import passroutebackend.debate.dto.response.DebateTurnSummary;
import com.fasterxml.jackson.core.type.TypeReference;
import passroutebackend.debate.entity.AiCompetitor;
import passroutebackend.debate.entity.AiPersona;
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
import passroutebackend.interview.dto.debate.InterviewerOpeningRequest;
import passroutebackend.interview.dto.debate.InterviewerOpeningResponse;
import passroutebackend.interview.dto.debate.PersonaPayload;
import passroutebackend.interview.dto.debate.TopicDetailAiRequest;
import passroutebackend.interview.dto.debate.TopicDetailAiResponse;
import passroutebackend.interview.dto.debate.TopicSuggestAiRequest;
import passroutebackend.interview.dto.debate.TopicSuggestAiResponse;

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
  public DebateTopicSuggestResponse suggestTopics(DebateTopicSuggestRequest req) {
    List<String> keywords = (req.getKeywords() == null)
        ? List.of()
        : req.getKeywords().stream().filter(k -> k != null && !k.isBlank()).toList();
    int count = (req.getCount() == null) ? 3 : req.getCount();

    TopicSuggestAiResponse ai = aiServerClient.suggestDebateTopics(
        TopicSuggestAiRequest.builder()
            .keywords(keywords)
            .count(count)
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
  public DebateTopicResponse generateTopic(DebateTopicGenerateRequest req) {
    TopicDetailAiResponse ai = aiServerClient.generateDebateTopicDetail(
        TopicDetailAiRequest.builder()
            .title(req.getTitle())
            .summary(req.getDescription())
            .category(req.getCategory())
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
    DebateSession session = transactionService.createSession(
        userId, topic, req.getUserStance(), persona, req.getDifficulty(),
        req.getMode(), prepSeconds);
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
    generateInterviewerOpeningAsync(sessionId, userId);
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
      log.warn("면접관 오프닝 생성 실패: sessionId={}, error={}", sessionId, e.getMessage());
    }
  }

  // ── 상태 조회 (폴링용) ─────────────────────────────────────────────────────

  public DebateStateResponse getState(Long userId, Long sessionId) {
    DebateSession session = transactionService.findSessionForUserOrThrow(sessionId, userId);
    boolean exposeEval = session.getMode() == DebateMode.PRACTICE;
    List<DebateTurnSummary> latestTurns = transactionService.findTurnsBySession(session).stream()
        .map(turn -> toTurnSummary(turn, exposeEval))
        .toList();
    return DebateStateResponse.builder()
        .sessionId(session.getId())
        .mode(session.getMode())
        .prepSeconds(session.getPrepSeconds())
        .currentState(session.getCurrentState())
        .isWaitingForUser(stateMachine.isWaitingForUser(session))
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

    String pendingStt = session.getPendingStt();
    boolean hasNewSpeech = pendingStt != null && !pendingStt.isBlank();

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
      Long turnId = transactionService.replaceUserTurn(session, round, userStance, pendingStt);
      session.updatePendingStt(null); // 저장은 아래에서 1회로 통합 (낙관적 락 충돌 방지)

      // 턴이 커밋된 뒤 평가 호출 → @Async가 새 턴을 확실히 조회한다.
      // 양 모드 공통 호출·누적 저장, 노출만 GET /state에서 분기 (PRACTICE 즉시 / REAL 종료 리포트).
      evaluationService.evaluateAsync(
          sessionId, userId, turnId,
          pendingStt, round, session.getUserStance(),
          topicTitle, opponentPreviousTurn);
    }

    // 시도(commit=false, PRACTICE 한정): 라운드를 확정하지 않고 재시도 여지를 둔 채 종료.
    if (!commit) {
      if (!hasNewSpeech) {
        // 새 발화도 없고 확정도 아닌 빈 요청.
        throw CustomException.of(ErrorCode.INVALID_INPUT);
      }
      transactionService.saveSession(session); // pendingStt 소비만 반영 (상태 전이 없음)
      return;
    }

    // 확정(commit=true): 제출된 발화가 반드시 존재해야 한다.
    if (!hasNewSpeech && !transactionService.existsUserTurn(session, round)) {
      throw CustomException.of(ErrorCode.DEBATE_NO_TURN_TO_COMMIT);
    }

    // pendingStt 소비 + 라운드 lock(*_USER → *_AI)을 세션 저장 1회로 함께 반영한다.
    // detached 세션을 두 번 저장하면 @Version 낙관적 락 충돌이 나므로 반드시 1회로 통합.
    stateMachine.onUserTurnSubmitted(session);
    transactionService.saveSession(session);

    // AI 경쟁자 답변 생성
    generateAiCompetitorTurnAsync(sessionId, userId, round);
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

      // CLOSING_AI 완료 후 자동으로 INTERVIEWER_CLOSING 생성 트리거
      if (session.getCurrentState() == DebateState.INTERVIEWER_CLOSING) {
        generateInterviewerClosingAsync(sessionId, userId);
      }

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

  private List<String> parseStringList(String json) {
    if (json == null) return List.of();
    return transactionService.parseJson(json, new com.fasterxml.jackson.core.type.TypeReference<>() {});
  }
}
