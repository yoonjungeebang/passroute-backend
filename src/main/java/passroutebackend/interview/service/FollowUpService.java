package passroutebackend.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import passroutebackend.interview.client.AiServerClient;
import passroutebackend.interview.dto.AnswerSubmitRequest;
import passroutebackend.interview.dto.AnswerSubmitResponse;
import passroutebackend.interview.dto.FollowUpRequest;
import passroutebackend.interview.dto.FollowUpResponse;
import passroutebackend.interview.entity.InterviewQuestion;

@Service
@RequiredArgsConstructor
public class FollowUpService {

  private final AiServerClient aiServerClient;
  private final FollowUpTransactionService transactionService;

  public AnswerSubmitResponse submitAnswer(AnswerSubmitRequest request) {
    FollowUpRequest followUpRequest = transactionService.saveAnswerAndBuildRequest(request);

    if (followUpRequest == null) {
      return AnswerSubmitResponse.noFollowUp();
    }

    FollowUpResponse aiResponse = aiServerClient.requestFollowUp(followUpRequest);

    if (!aiResponse.isHasFollowUp()) {
      return AnswerSubmitResponse.noFollowUp();
    }

    InterviewQuestion followUpQuestion = transactionService.saveFollowUpQuestion(
        request.getQuestionId(), aiResponse.getFollowUpQuestion()
    );

    return AnswerSubmitResponse.followUp(followUpQuestion.getId(), followUpQuestion.getQuestionText());
  }
}
