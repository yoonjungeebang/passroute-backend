package passroutebackend.interview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.dto.evaluation.LlmScores;
import passroutebackend.interview.entity.InterviewAnswer;
import passroutebackend.interview.entity.InterviewQuestion;
import passroutebackend.interview.entity.InterviewReadiness;
import passroutebackend.interview.entity.InterviewReport;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.entity.ReportStatus;
import passroutebackend.interview.entity.SessionStatus;
import passroutebackend.interview.entity.FaceAnalysis;
import passroutebackend.interview.entity.VoiceAnalysis;
import passroutebackend.interview.repository.FaceAnalysisRepository;
import passroutebackend.interview.repository.InterviewAnswerRepository;
import passroutebackend.interview.repository.InterviewQuestionRepository;
import passroutebackend.interview.repository.InterviewReportRepository;
import passroutebackend.interview.repository.InterviewSessionRepository;
import passroutebackend.interview.repository.VoiceAnalysisRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportTransactionService {

  private final InterviewSessionRepository sessionRepository;
  private final InterviewQuestionRepository questionRepository;
  private final InterviewAnswerRepository answerRepository;
  private final InterviewReportRepository reportRepository;
  private final VoiceAnalysisRepository voiceAnalysisRepository;
  private final FaceAnalysisRepository faceAnalysisRepository;
  private final ObjectMapper objectMapper;

  @Transactional(readOnly = true)
  public ReportContext loadContext(Long sessionId) {
    InterviewSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> CustomException.of(ErrorCode.SESSION_NOT_FOUND));
    InterviewRoom room = session.getInterviewRoom();

    List<InterviewQuestion> questions = questionRepository.findBySessionOrderByQuestionOrderAsc(session);
    List<InterviewAnswer> answers = answerRepository.findByQuestionIn(questions);

    Map<Long, InterviewAnswer> answerByQuestionId = answers.stream()
        .collect(Collectors.toMap(a -> a.getQuestion().getId(), Function.identity()));

    List<QuestionAnswerData> questionAnswers = new java.util.ArrayList<>();
    int index = 1;
    for (InterviewQuestion q : questions) {
      InterviewAnswer answer = answerByQuestionId.get(q.getId());
      if (answer == null || answer.getPercentage() == null) continue;
      String llmScoresJson = answer.getLlmScores();
      LlmScores llmScores = parseLlmScores(llmScoresJson);
      questionAnswers.add(new QuestionAnswerData(
          index++,
          q.getQuestionText(),
          answer.getAnswerText(),
          answer.getPercentage(),
          answer.getStarScore(),
          llmScoresJson,
          llmScores,
          answer.getConcisenessFinal()
      ));
    }

    return new ReportContext(
        sessionId,
        room.getJobPosition(),
        room.getCompanyName(),
        room.getInterviewType().getValue(),
        questionAnswers
    );
  }

  @Transactional(readOnly = true)
  public long countAnswers(Long sessionId) {
    return answerRepository.countBySessionId(sessionId);
  }

  @Transactional(readOnly = true)
  public long countEvaluatedAnswers(Long sessionId) {
    return answerRepository.countEvaluatedBySessionId(sessionId);
  }

  @Transactional(readOnly = true)
  public Optional<InterviewReport> findReport(Long sessionId, Long userId) {
    InterviewSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> CustomException.of(ErrorCode.SESSION_NOT_FOUND));
    if (!session.getInterviewRoom().getUserId().equals(userId)) {
      throw CustomException.of(ErrorCode.ACCESS_DENIED);
    }
    if (session.getStatus() != SessionStatus.COMPLETED) {
      throw CustomException.of(ErrorCode.SESSION_NOT_ENDED);
    }
    return reportRepository.findBySession(session);
  }

  @Transactional(readOnly = true)
  public Optional<InterviewReport> findReport(Long sessionId) {
    InterviewSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> CustomException.of(ErrorCode.SESSION_NOT_FOUND));
    return reportRepository.findBySession(session);
  }

  // 생성 시작 시점에 GENERATING row를 별도 트랜잭션으로 즉시 커밋(폴러 가시성 확보).
  // 이미 리포트가 있으면(완료/실패/동시진행) false 반환 → 중복 생성 스킵.
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean startGeneratingReport(Long sessionId) {
    InterviewSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> CustomException.of(ErrorCode.SESSION_NOT_FOUND));
    if (reportRepository.findBySession(session).isPresent()) {
      return false;
    }
    try {
      // saveAndFlush로 즉시 insert → 동시 트리거 시 unique(session_id) 위반을 여기서 잡는다.
      reportRepository.saveAndFlush(InterviewReport.builder()
          .session(session)
          .reportStatus(ReportStatus.GENERATING)
          .build());
      return true;
    } catch (DataIntegrityViolationException e) {
      // 다른 스레드가 거의 동시에 먼저 생성함 → 중복 생성 스킵(우아하게 처리)
      log.info("리포트 동시 생성 시도 감지, 중복 생성 스킵 sessionId={}", sessionId);
      return false;
    }
  }

  @Transactional
  public void saveReport(Long sessionId, double sessionScore, InterviewReadiness interviewReadiness,
      String overall, String strengths, String weaknessesJson, String improvements,
      String questionFeedbackJson, String recommendedQuestionsJson, String finalAdvice,
      String readinessComment, String keyWeaknessJson, String itemAveragesJson,
      Double voiceScore, Double faceScore,
      Double avgWpm, Double avgSilenceDuration, Integer fillerCount,
      Double avgGazeRatio, Integer gazeOffCount, Double avgBlinkPerMin) {
    InterviewSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> CustomException.of(ErrorCode.SESSION_NOT_FOUND));
    InterviewReport report = reportRepository.findBySessionForUpdate(session).orElse(null);
    // 워치독이 이미 FAILED로 선점했거나 행이 없으면 완료 결과를 덮어쓰지 않는다(부활 방지).
    if (report == null || report.getReportStatus() != ReportStatus.GENERATING) {
      log.warn("리포트가 GENERATING 상태가 아니어서 완료 저장 생략, sessionId={}", sessionId);
      return;
    }
    report.completeReport(sessionScore, interviewReadiness, overall, strengths, weaknessesJson,
        improvements, questionFeedbackJson, recommendedQuestionsJson, finalAdvice, readinessComment,
        keyWeaknessJson, itemAveragesJson, voiceScore, faceScore,
        avgWpm, avgSilenceDuration, fillerCount, avgGazeRatio, gazeOffCount, avgBlinkPerMin);
  }

  @Transactional
  public void saveFailedReport(Long sessionId) {
    InterviewSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> CustomException.of(ErrorCode.SESSION_NOT_FOUND));
    InterviewReport report = reportRepository.findBySessionForUpdate(session).orElse(null);
    // GENERATING row가 없는 예외적 경우(시작 insert 누락 등)만 FAILED로 새로 기록.
    if (report == null) {
      reportRepository.save(InterviewReport.builder()
          .session(session)
          .reportStatus(ReportStatus.FAILED)
          .build());
      return;
    }
    // 이미 COMPLETED면 건드리지 않고, GENERATING일 때만 FAILED로 전환.
    if (report.getReportStatus() == ReportStatus.GENERATING) {
      report.markFailed();
    }
  }

  // 워치독: stale GENERATING을 FAILED로 조건부 전환. 실제 전환되면 true.
  @Transactional
  public boolean markStaleAsFailed(Long reportId) {
    return reportRepository.markStaleAsFailed(reportId) > 0;
  }

  @Transactional(readOnly = true)
  public List<VoiceAnalysis> loadVoiceAnalysis(Long sessionId) {
    return voiceAnalysisRepository.findBySessionId(sessionId);
  }

  @Transactional(readOnly = true)
  public List<FaceAnalysis> loadFaceAnalysis(Long sessionId) {
    return faceAnalysisRepository.findBySessionId(sessionId);
  }

  @Transactional(readOnly = true)
  public InterviewSession loadSession(Long sessionId) {
    return sessionRepository.findById(sessionId)
        .orElseThrow(() -> CustomException.of(ErrorCode.SESSION_NOT_FOUND));
  }

  private LlmScores parseLlmScores(String json) {
    if (json == null) return null;
    try {
      return objectMapper.readValue(json, LlmScores.class);
    } catch (Exception e) {
      log.warn("LlmScores 역직렬화 실패: {}", e.getMessage());
      return null;
    }
  }
}
