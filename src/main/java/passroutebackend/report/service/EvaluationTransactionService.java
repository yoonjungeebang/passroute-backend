package passroutebackend.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.dto.EvaluationContext;
import passroutebackend.interview.entity.InterviewAnswer;
import passroutebackend.interview.entity.InterviewQuestion;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.repository.InterviewAnswerRepository;
import passroutebackend.interview.repository.InterviewQuestionRepository;

@Service
@RequiredArgsConstructor
public class EvaluationTransactionService {

  private final InterviewAnswerRepository answerRepository;
  private final InterviewQuestionRepository questionRepository;

  // 트랜잭션 안에서 Lazy 연관관계 전부 탐색 후 단순 데이터로 반환
  @Transactional(readOnly = true)
  public EvaluationContext loadContext(Long questionId) {
    InterviewQuestion question = questionRepository.findById(questionId)
        .orElseThrow(() -> CustomException.of(ErrorCode.QUESTION_NOT_FOUND));
    InterviewAnswer answer = answerRepository.findByQuestion(question)
        .orElseThrow(() -> CustomException.of(ErrorCode.ANSWER_NOT_FOUND));
    InterviewRoom room = question.getSession().getInterviewRoom();

    return new EvaluationContext(
        answer.getId(),
        question.getQuestionText(),
        answer.getAnswerText(),
        room.getInterviewType().getValue(),
        room.getDifficulty().getValue(),
        room.getJobPosition(),
        room.getCompanyName()
    );
  }

  @Transactional
  public void saveResult(Long answerId, Double percentage, Integer starScore, String llmScoresJson, Double concisenessFinal) {
    InterviewAnswer answer = answerRepository.findById(answerId)
        .orElseThrow(() -> CustomException.of(ErrorCode.ANSWER_NOT_FOUND));
    answer.updateEvaluationResult(percentage, starScore, llmScoresJson, concisenessFinal);
  }
}