package passroutebackend.interview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
    InterviewReport report = InterviewReport.builder()
        .session(session)
        .sessionScore(sessionScore)
        .interviewReadiness(interviewReadiness)
        .overall(overall)
        .strengths(strengths)
        .weaknesses(weaknessesJson)
        .improvements(improvements)
        .questionFeedback(questionFeedbackJson)
        .recommendedQuestions(recommendedQuestionsJson)
        .finalAdvice(finalAdvice)
        .readinessComment(readinessComment)
        .keyWeakness(keyWeaknessJson)
        .itemAverages(itemAveragesJson)
        .reportStatus(ReportStatus.COMPLETED)
        .voiceScore(voiceScore)
        .faceScore(faceScore)
        .avgWpm(avgWpm)
        .avgSilenceDuration(avgSilenceDuration)
        .fillerCount(fillerCount)
        .avgGazeRatio(avgGazeRatio)
        .gazeOffCount(gazeOffCount)
        .avgBlinkPerMin(avgBlinkPerMin)
        .build();
    reportRepository.save(report);
  }

  @Transactional
  public void saveFailedReport(Long sessionId) {
    InterviewSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> CustomException.of(ErrorCode.SESSION_NOT_FOUND));
    if (reportRepository.findBySession(session).isPresent()) {
      return;
    }
    InterviewReport report = InterviewReport.builder()
        .session(session)
        .reportStatus(ReportStatus.FAILED)
        .build();
    reportRepository.save(report);
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
