package passroutebackend.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.global.property.FollowUpProperties;
import passroutebackend.interview.client.AiServerClient;
import passroutebackend.interview.dto.AnswerSubmitRequest;
import passroutebackend.interview.dto.AnswerSubmitResponse;
import passroutebackend.interview.dto.FollowUpRequest;
import passroutebackend.interview.dto.FollowUpResponse;
import passroutebackend.interview.dto.QATurn;
import passroutebackend.interview.entity.InterviewAnswer;
import passroutebackend.interview.entity.InterviewQuestion;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.repository.InterviewAnswerRepository;
import passroutebackend.interview.repository.InterviewQuestionRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowUpService {

  private static final List<String> SKIP_KEYWORDS = List.of(
      "자기소개", "본인 소개",
      "지원 동기", "지원동기", "왜 지원",
      "마지막으로 하고 싶은", "마지막 한마디",
      "입사 후 포부",
      "장점", "단점", "장단점"
  );

  private final FollowUpProperties followUpProperties;
  private final AiServerClient aiServerClient;
  private final InterviewQuestionRepository questionRepository;
  private final InterviewAnswerRepository answerRepository;

  public AnswerSubmitResponse submitAnswer(AnswerSubmitRequest request) {
    InterviewQuestion question = questionRepository.findById(request.getQuestionId())
        .orElseThrow(() -> CustomException.of(ErrorCode.QUESTION_NOT_FOUND));

    InterviewAnswer answer = InterviewAnswer.builder()
        .question(question)
        .answerText(request.getAnswerText())
        .build();
    answerRepository.save(answer);

    InterviewSession session = question.getSession();
    int setNumber = question.getSetNumber();
    String originalQuestionText = findOriginalQuestionText(session, setNumber);

    if (shouldSkipFollowUp(originalQuestionText)) {
      return AnswerSubmitResponse.noFollowUp();
    }

    if (isMaxTurnReached(session, setNumber)) {
      return AnswerSubmitResponse.noFollowUp();
    }

    List<QATurn> conversation = buildConversation(session, setNumber);
    InterviewRoom room = session.getInterviewRoom();

    FollowUpRequest followUpRequest = new FollowUpRequest(
        room.getInterviewType().getValue(),
        room.getDifficulty().getValue(),
        conversation
    );

    FollowUpResponse aiResponse = aiServerClient.requestFollowUp(followUpRequest);

    if (!aiResponse.isHasFollowUp()) {
      return AnswerSubmitResponse.noFollowUp();
    }

    InterviewQuestion followUpQuestion = createFollowUpQuestion(
        session, setNumber, aiResponse.getFollowUpQuestion(), conversation.size()
    );

    return AnswerSubmitResponse.followUp(followUpQuestion.getId(), followUpQuestion.getQuestionText());
  }

  private String findOriginalQuestionText(InterviewSession session, int setNumber) {
    return questionRepository.findBySessionAndSetNumberOrderByQuestionOrderAsc(session, setNumber)
        .stream()
        .filter(q -> !q.isFollowUp())
        .findFirst()
        .map(InterviewQuestion::getQuestionText)
        .orElse("");
  }

  private boolean shouldSkipFollowUp(String questionText) {
    return SKIP_KEYWORDS.stream()
        .anyMatch(questionText::contains);
  }

  private boolean isMaxTurnReached(InterviewSession session, int setNumber) {
    int followUpCount = questionRepository.countBySessionAndSetNumberAndFollowUpTrue(session, setNumber);
    return followUpCount >= followUpProperties.getMaxTurn();
  }

  private List<QATurn> buildConversation(InterviewSession session, int setNumber) {
    List<InterviewQuestion> questions =
        questionRepository.findBySessionAndSetNumberOrderByQuestionOrderAsc(session, setNumber);

    List<QATurn> conversation = new ArrayList<>();
    for (InterviewQuestion q : questions) {
      String answerText = answerRepository.findByQuestion(q)
          .map(InterviewAnswer::getAnswerText)
          .orElse("");
      conversation.add(new QATurn(q.getQuestionText(), answerText));
    }
    return conversation;
  }

  private InterviewQuestion createFollowUpQuestion(InterviewSession session, int setNumber,
      String questionText, int order) {
    InterviewQuestion followUpQuestion = InterviewQuestion.builder()
        .session(session)
        .setNumber(setNumber)
        .questionText(questionText)
        .questionOrder(order)
        .followUp(true)
        .build();
    return questionRepository.save(followUpQuestion);
  }
}
