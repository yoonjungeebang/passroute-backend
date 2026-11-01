package passroutebackend.debate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import passroutebackend.debate.dto.ai.common.DebateTurnItem;
import passroutebackend.debate.dto.ai.common.PersonaPayload;
import passroutebackend.debate.dto.ai.request.DebateClosingRequest;
import passroutebackend.debate.dto.ai.request.DebateOpeningRequest;
import passroutebackend.debate.dto.ai.request.DebateRebuttalRequest;
import passroutebackend.debate.dto.ai.request.InterviewerClosingRequest;
import passroutebackend.debate.dto.ai.request.InterviewerCueRequest;
import passroutebackend.debate.dto.ai.request.InterviewerOpeningRequest;
import passroutebackend.debate.dto.ai.response.DebateClosingResponse;
import passroutebackend.debate.dto.ai.response.DebateOpeningResponse;
import passroutebackend.debate.dto.ai.response.DebateRebuttalResponse;
import passroutebackend.debate.dto.ai.response.InterviewerClosingResponse;
import passroutebackend.debate.dto.ai.response.InterviewerCueResponse;
import passroutebackend.debate.dto.ai.response.InterviewerOpeningResponse;
import passroutebackend.debate.dto.ai.type.InterviewerCueType;
import passroutebackend.debate.entity.AiCompetitor;
import passroutebackend.debate.entity.AiPersona;
import passroutebackend.debate.entity.DebateRound;
import passroutebackend.debate.entity.DebateSession;
import passroutebackend.debate.entity.DebateStance;
import passroutebackend.debate.entity.DebateState;
import passroutebackend.debate.entity.DebateTopic;
import passroutebackend.debate.entity.DebateTurn;
import passroutebackend.debate.entity.SpeakerType;
import passroutebackend.debate.entity.TurnStance;
import passroutebackend.global.client.AiServerClient;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;

import java.util.List;

/**
 * 토론 면접의 비동기 AI 응답 생성 담당.
 * 면접관 오프닝/클로징/cue, AI 경쟁자 답변 생성을 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DebateAiGenerationService {

  private final DebateTransactionService transactionService;
  private final DebateStateMachine stateMachine;
  private final AiServerClient aiServerClient;
  /**
   * 자기 자신 프록시. @Async 자가호출은 프록시를 안 거쳐 동기 실행되므로,
   * 컨트롤러 스레드에서 비동기 작업을 띄울 때는 이 프록시를 통해 호출한다.
   * ObjectProvider는 지연 조회라 자기 참조 순환 주입 문제가 없다.
   */
  private final ObjectProvider<DebateAiGenerationService> selfProvider;

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
   * 현재 상태 -> cue_type 매핑하여 AI 호출 -> AI_INTERVIEWER MODERATION 턴 저장 -> 다음 사용자 턴으로 전이.
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
      stateMachine.onAiTurnCompleted(session); // *_CUE -> 다음 *_USER
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
      default -> { /* REBUTTAL_1_DECISION 등: 사용자 입력/선택 대기 -> 트리거 없음 */ }
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

  // ── private helpers ──────────────────────────────────────────────────────

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

  private TurnStance toTurnStance(DebateStance stance) {
    return stance == DebateStance.PRO ? TurnStance.PRO : TurnStance.CON;
  }

  private List<String> parseStringList(String json) {
    if (json == null) return List.of();
    return transactionService.parseJson(json, new com.fasterxml.jackson.core.type.TypeReference<>() {});
  }
}