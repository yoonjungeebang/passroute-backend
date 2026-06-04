package passroutebackend.debate.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.debate.entity.AiCompetitor;
import passroutebackend.debate.entity.AiPersona;
import passroutebackend.debate.entity.DebateReport;
import passroutebackend.debate.entity.DebateRound;
import passroutebackend.debate.entity.DebateSession;
import passroutebackend.debate.entity.DebateStance;
import passroutebackend.debate.entity.DebateTopic;
import passroutebackend.debate.entity.DebateTurn;
import passroutebackend.debate.entity.SpeakerType;
import passroutebackend.debate.entity.TopicCategory;
import passroutebackend.debate.entity.TurnStance;
import passroutebackend.debate.repository.AiCompetitorRepository;
import passroutebackend.debate.repository.AiPersonaRepository;
import passroutebackend.debate.repository.DebateReportRepository;
import passroutebackend.debate.repository.DebateSessionRepository;
import passroutebackend.debate.repository.DebateTopicRepository;
import passroutebackend.debate.repository.DebateTurnRepository;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.entity.Difficulty;
import passroutebackend.interview.entity.ReportStatus;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebateTransactionService {

  private final DebateSessionRepository sessionRepository;
  private final DebateTopicRepository topicRepository;
  private final AiPersonaRepository personaRepository;
  private final AiCompetitorRepository competitorRepository;
  private final DebateTurnRepository turnRepository;
  private final DebateReportRepository reportRepository;
  private final ObjectMapper objectMapper;

  // ── 조회 ──────────────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public List<DebateTopic> findAllTopics() {
    return topicRepository.findByGeneratedFalse();
  }

  @Transactional(readOnly = true)
  public List<DebateTopic> findTopicsByCategory(TopicCategory category) {
    return topicRepository.findByCategoryAndGeneratedFalse(category);
  }

  @Transactional(readOnly = true)
  public List<AiPersona> findAllPersonas() {
    return personaRepository.findAll();
  }

  @Transactional(readOnly = true)
  public DebateTopic findTopicOrThrow(Long topicId) {
    return topicRepository.findById(topicId)
        .orElseThrow(() -> CustomException.of(ErrorCode.DEBATE_TOPIC_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public AiPersona findPersonaOrThrow(Long personaId) {
    return personaRepository.findById(personaId)
        .orElseThrow(() -> CustomException.of(ErrorCode.PERSONA_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public DebateSession findSessionForUserOrThrow(Long sessionId, Long userId) {
    return sessionRepository.findByIdAndUserId(sessionId, userId)
        .orElseThrow(() -> CustomException.of(ErrorCode.DEBATE_SESSION_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public List<DebateTurn> findTurnsBySession(DebateSession session) {
    return turnRepository.findBySessionOrderByCreatedAtAsc(session);
  }

  @Transactional(readOnly = true)
  public List<DebateTurn> findCompetitorTurnsBySession(DebateSession session) {
    return turnRepository.findBySessionAndSpeakerTypeOrderByCreatedAtAsc(
        session, SpeakerType.AI_COMPETITOR);
  }

  @Transactional(readOnly = true)
  public Optional<DebateReport> findReportBySession(DebateSession session) {
    return reportRepository.findBySession(session);
  }

  // ── AI 생성 주제 저장 ─────────────────────────────────────────────────────

  @Transactional
  public DebateTopic saveGeneratedTopic(String topicKey, String title, String description,
      TopicCategory category, String proKeyPoints, String conKeyPoints) {
    DebateTopic topic = DebateTopic.builder()
        .topicKey(topicKey)
        .title(title)
        .description(description)
        .category(category)
        .proKeyPoints(proKeyPoints)
        .conKeyPoints(conKeyPoints)
        .generated(true)
        .build();
    return topicRepository.save(topic);
  }

  // ── 세션 생성 / 시작 / 종료 ────────────────────────────────────────────────

  @Transactional
  public DebateSession createSession(Long userId, DebateTopic topic, DebateStance userStance,
      AiPersona persona, Difficulty difficulty) {
    DebateSession session = DebateSession.builder()
        .userId(userId)
        .topic(topic)
        .userStance(userStance)
        .difficulty(difficulty)
        .build();
    sessionRepository.save(session);

    DebateStance aiStance = (userStance == DebateStance.PRO) ? DebateStance.CON : DebateStance.PRO;
    AiCompetitor competitor = AiCompetitor.builder()
        .session(session)
        .persona(persona)
        .stance(aiStance)
        .build();
    competitorRepository.save(competitor);

    return session;
  }

  @Transactional
  public DebateSession saveSession(DebateSession session) {
    return sessionRepository.save(session);
  }

  // ── 턴 저장 ──────────────────────────────────────────────────────────────

  @Transactional
  public DebateTurn saveTurn(DebateSession session, SpeakerType speakerType,
      AiCompetitor competitor, TurnStance stance, DebateRound round, String content,
      String audioUrl) {
    DebateTurn turn = DebateTurn.builder()
        .session(session)
        .speakerType(speakerType)
        .competitor(competitor)
        .stance(stance)
        .round(round)
        .content(content)
        .audioUrl(audioUrl)
        .build();
    return turnRepository.save(turn);
  }

  @Transactional
  public void updateTurnEvaluation(Long turnId, Double weightedScore,
      String llmScoresJson, String evalSummaryJson) {
    DebateTurn turn = turnRepository.findById(turnId)
        .orElseThrow(() -> CustomException.of(ErrorCode.INVALID_INPUT));
    turn.updateEvaluation(weightedScore, llmScoresJson, evalSummaryJson);
  }

  // ── 리포트 저장 ───────────────────────────────────────────────────────────

  @Transactional
  public void saveReport(DebateSession session, Double sessionScore, ReportStatus status,
      String overall, String strengths, String weaknessesJson, String improvements,
      String turnFeedbackJson, String strategyAnalysis, String recommendedTopicsJson,
      String finalAdvice, String debateReadinessComment) {
    DebateReport report = DebateReport.builder()
        .session(session)
        .sessionScore(sessionScore)
        .reportStatus(status)
        .overall(overall)
        .strengths(strengths)
        .weaknesses(weaknessesJson)
        .improvements(improvements)
        .turnFeedback(turnFeedbackJson)
        .strategyAnalysis(strategyAnalysis)
        .recommendedTopics(recommendedTopicsJson)
        .finalAdvice(finalAdvice)
        .debateReadinessComment(debateReadinessComment)
        .build();
    reportRepository.save(report);
  }

  @Transactional
  public void saveFailedReport(DebateSession session) {
    if (reportRepository.findBySession(session).isPresent()) {
      return;
    }
    DebateReport report = DebateReport.builder()
        .session(session)
        .reportStatus(ReportStatus.FAILED)
        .build();
    reportRepository.save(report);
  }

  // ── JSON 직렬화/역직렬화 유틸 ───────────────────────────────────────────────

  public String toJson(Object obj) {
    if (obj == null) return null;
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      log.warn("JSON 직렬화 실패: {}", e.getMessage());
      return null;
    }
  }

  public <T> T parseJson(String json, TypeReference<T> typeReference) {
    if (json == null) return null;
    try {
      return objectMapper.readValue(json, typeReference);
    } catch (Exception e) {
      log.warn("JSON 역직렬화 실패: {}", e.getMessage());
      return null;
    }
  }
}
