package passroutebackend.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.dto.response.QuestionDto;
import passroutebackend.interview.dto.response.SessionQuestionListResponse;
import passroutebackend.interview.entity.InterviewAnswer;
import passroutebackend.interview.entity.InterviewQuestion;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.entity.RoomStatus;
import passroutebackend.interview.entity.SessionStatus;
import passroutebackend.interview.repository.InterviewAnswerRepository;
import passroutebackend.interview.repository.InterviewQuestionRepository;
import passroutebackend.interview.repository.InterviewSessionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewSessionTxService {

  private final InterviewSessionRepository sessionRepository;
  private final InterviewQuestionRepository questionRepository;
  private final InterviewAnswerRepository answerRepository;

  @Transactional(readOnly = true)
  public SessionQuestionListResponse getQuestions(Long sessionId, Long userId) {
    InterviewSession session = findAndValidateOwnership(sessionId, userId);

    List<QuestionDto> questions = questionRepository.findBySessionOrderByQuestionOrderAsc(session)
        .stream()
        .map(q -> new QuestionDto(q.getId(), q.getQuestionText(), q.getQuestionOrder()))
        .toList();

    return new SessionQuestionListResponse(questions);
  }

  @Transactional(readOnly = true)
  public void validateSessionForAnswer(Long sessionId, Long userId) {
    InterviewSession session = findAndValidateOwnership(sessionId, userId);

    if (session.getStatus() == SessionStatus.COMPLETED) {
      throw CustomException.of(ErrorCode.SESSION_ALREADY_ENDED);
    }
  }

  @Transactional(readOnly = true)
  public boolean computeIsLastQuestion(Long sessionId, boolean hasFollowUp) {
    if (hasFollowUp) {
      return false;
    }

    InterviewSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> CustomException.of(ErrorCode.SESSION_NOT_FOUND));

    List<InterviewQuestion> allQuestions =
        questionRepository.findBySessionOrderByQuestionOrderAsc(session);
    List<InterviewAnswer> answers = answerRepository.findByQuestionIn(allQuestions);

    return answers.size() >= allQuestions.size();
  }

  @Transactional
  public void endSession(Long sessionId, Long userId) {
    InterviewSession session = findAndValidateOwnership(sessionId, userId);

    if (session.getStatus() == SessionStatus.COMPLETED) {
      throw CustomException.of(ErrorCode.SESSION_ALREADY_ENDED);
    }

    session.end(SessionStatus.COMPLETED);
    session.getInterviewRoom().updateStatus(RoomStatus.COMPLETED);
  }

  @Transactional(readOnly = true)
  public String getWorstClipVideoUrl(Long sessionId, Long userId) {
    InterviewSession session = findAndValidateOwnership(sessionId, userId);
    List<InterviewAnswer> answers = answerRepository.findBySessionOrderByClipScoreAsc(session);
    if (answers.isEmpty()) {
      return null;
    }
    return answers.get(0).getVideoUrl();
  }

  private InterviewSession findAndValidateOwnership(Long sessionId, Long userId) {
    InterviewSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> CustomException.of(ErrorCode.SESSION_NOT_FOUND));

    if (!session.getInterviewRoom().getUserId().equals(userId)) {
      throw CustomException.of(ErrorCode.ACCESS_DENIED);
    }

    return session;
  }
}
