package passroutebackend.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.dto.report.InterviewReportResponse;
import passroutebackend.interview.entity.InterviewAnswer;
import passroutebackend.interview.entity.InterviewQuestion;
import passroutebackend.interview.entity.InterviewReadiness;
import passroutebackend.interview.entity.InterviewReport;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.entity.SessionStatus;
import passroutebackend.interview.repository.InterviewAnswerRepository;
import passroutebackend.interview.repository.InterviewQuestionRepository;
import passroutebackend.interview.repository.InterviewReportRepository;
import passroutebackend.interview.repository.InterviewSessionRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportTransactionService {

  private final InterviewSessionRepository sessionRepository;
  private final InterviewQuestionRepository questionRepository;
  private final InterviewAnswerRepository answerRepository;
  private final InterviewReportRepository reportRepository;

  @Transactional
  public void endSession(Long sessionId) {
    InterviewSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> CustomException.of(ErrorCode.SESSION_NOT_FOUND));
    session.end(SessionStatus.COMPLETED);
  }

  @Transactional(readOnly = true)
  public ReportContext loadContext(Long sessionId) {
    InterviewSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> CustomException.of(ErrorCode.SESSION_NOT_FOUND));
    InterviewRoom room = session.getInterviewRoom();

    List<InterviewQuestion> questions = questionRepository.findBySession(session);
    List<InterviewAnswer> answers = answerRepository.findByQuestionIn(questions);

    Map<Long, InterviewAnswer> answerByQuestionId = answers.stream()
        .collect(Collectors.toMap(a -> a.getQuestion().getId(), Function.identity()));

    List<QuestionAnswerData> questionAnswers = new java.util.ArrayList<>();
    int index = 1;
    for (InterviewQuestion q : questions) {
      InterviewAnswer answer = answerByQuestionId.get(q.getId());
      if (answer == null || answer.getPercentage() == null) continue;
      questionAnswers.add(new QuestionAnswerData(
          index++,
          q.getQuestionText(),
          answer.getAnswerText(),
          answer.getPercentage(),
          answer.getStarScore(),
          answer.getLlmScores(),
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
  public Optional<InterviewReport> findReport(Long sessionId) {
    InterviewSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> CustomException.of(ErrorCode.SESSION_NOT_FOUND));
    return reportRepository.findBySession(session);
  }

  @Transactional
  public void saveReport(Long sessionId, double sessionScore, InterviewReadiness interviewReadiness,
      String overall, String strengths, String weaknessesJson, String improvements,
      String questionFeedbackJson, String recommendedQuestionsJson, String finalAdvice,
      String readinessComment, String keyWeaknessJson, String itemAveragesJson) {
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
        .build();
    reportRepository.save(report);
  }
}
