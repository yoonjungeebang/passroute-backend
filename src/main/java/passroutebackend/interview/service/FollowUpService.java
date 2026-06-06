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
  private final EvaluationService evaluationService;

  public AnswerSubmitResponse submitAnswer(AnswerSubmitRequest request) {
    FollowUpRequest followUpRequest = transactionService.saveAnswerAndBuildRequest(request);

    // 답변 저장 완료 후 평가는 백그라운드에서 비동기 실행
    evaluationService.evaluateAsync(request.getQuestionId(), request.getVoiceData());

    if (followUpRequest == null) {
      return AnswerSubmitResponse.noFollowUp();
    }

    FollowUpResponse aiResponse = aiServerClient.requestFollowUp(followUpRequest);

    if (!aiResponse.isHasFollowUp()) {
      return AnswerSubmitResponse.noFollowUp();
    }

    InterviewQuestion followUpQuestion = transactionService.saveFollowUpQuestion(
        request.getQuestionId(), aiResponse.getFollowUpQuestion(), aiResponse.getAudioUrl()
    );

    return AnswerSubmitResponse.followUp(
        followUpQuestion.getId(), followUpQuestion.getQuestionText(), followUpQuestion.getAudioUrl());
  }
}
