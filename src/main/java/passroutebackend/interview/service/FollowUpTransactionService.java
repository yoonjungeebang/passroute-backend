package passroutebackend.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.global.property.FollowUpProperties;
import passroutebackend.interview.dto.AnswerSubmitRequest;
import passroutebackend.interview.dto.FollowUpRequest;
import passroutebackend.interview.dto.QATurn;
import passroutebackend.interview.entity.InterviewAnswer;
import passroutebackend.interview.entity.InterviewQuestion;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.repository.InterviewAnswerRepository;
import passroutebackend.interview.repository.InterviewQuestionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowUpTransactionService {

  private static final List<String> SKIP_KEYWORDS = List.of(
      "자기소개", "본인 소개",
      "지원 동기", "지원동기", "왜 지원",
      "마지막으로 하고 싶은", "마지막 한마디",
      "입사 후 포부",
      "장점", "단점", "장단점"
  );

  private final FollowUpProperties followUpProperties;
  private final InterviewQuestionRepository questionRepository;
  private final InterviewAnswerRepository answerRepository;

  public FollowUpRequest saveAnswerAndBuildRequest(AnswerSubmitRequest request) {
    InterviewQuestion question = questionRepository.findById(request.getQuestionId())
        .orElseThrow(() -> CustomException.of(ErrorCode.QUESTION_NOT_FOUND));

    InterviewAnswer answer = InterviewAnswer.builder()
        .question(question)
        .answerText(request.getAnswerText())
        .build();
    if (request.getVideoUrl() != null) {
      answer.updateVideoClip(request.getVideoUrl(), request.getClipScore(), request.getClipReason());
    }
    answerRepository.save(answer);

    InterviewSession session = question.getSession();
    int setNumber = question.getSetNumber();

    List<InterviewQuestion> questions =
        questionRepository.findBySessionAndSetNumberOrderByQuestionOrderAsc(session, setNumber);

    String originalQuestionText = findOriginalQuestionText(questions);

    if (shouldSkipFollowUp(originalQuestionText)) {
      return null;
    }

    if (isMaxTurnReached(questions)) {
      return null;
    }

    List<QATurn> conversation = buildConversation(questions);
    InterviewRoom room = session.getInterviewRoom();

    return new FollowUpRequest(
        room.getInterviewType().getValue(),
        room.getDifficulty().getValue(),
        conversation,
        room.getUserId(),
        room.getAiInterviewer()
    );
  }

  public InterviewQuestion saveFollowUpQuestion(Long questionId, String followUpQuestionText,
      String audioUrl) {
    InterviewQuestion question = questionRepository.findById(questionId)
        .orElseThrow(() -> CustomException.of(ErrorCode.QUESTION_NOT_FOUND));

    InterviewSession session = question.getSession();
    int setNumber = question.getSetNumber();

    List<InterviewQuestion> questions =
        questionRepository.findBySessionAndSetNumberOrderByQuestionOrderAsc(session, setNumber);
    int nextOrder = questions.size();

    InterviewQuestion followUpQuestion = InterviewQuestion.builder()
        .session(session)
        .setNumber(setNumber)
        .questionText(followUpQuestionText)
        .questionOrder(nextOrder)
        .followUp(true)
        .audioUrl(audioUrl)
        .build();
    return questionRepository.save(followUpQuestion);
  }

  private String findOriginalQuestionText(List<InterviewQuestion> questions) {
    return questions.stream()
        .filter(q -> !q.isFollowUp())
        .findFirst()
        .map(InterviewQuestion::getQuestionText)
        .orElse("");
  }

  private boolean shouldSkipFollowUp(String questionText) {
    return SKIP_KEYWORDS.stream()
        .anyMatch(questionText::contains);
  }

  private boolean isMaxTurnReached(List<InterviewQuestion> questions) {
    long followUpCount = questions.stream()
        .filter(InterviewQuestion::isFollowUp)
        .count();
    return followUpCount >= followUpProperties.getMaxTurn();
  }

  private List<QATurn> buildConversation(List<InterviewQuestion> questions) {
    Map<Long, InterviewAnswer> answerMap = answerRepository.findByQuestionIn(questions).stream()
        .collect(Collectors.toMap(a -> a.getQuestion().getId(), Function.identity()));

    List<QATurn> conversation = new ArrayList<>();
    for (InterviewQuestion q : questions) {
      InterviewAnswer answer = answerMap.get(q.getId());
      String answerText = answer != null ? answer.getAnswerText() : "";
      conversation.add(new QATurn(q.getQuestionText(), answerText));
    }
    return conversation;
  }
}
