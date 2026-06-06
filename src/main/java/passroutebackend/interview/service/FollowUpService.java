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

    // AI가 has_follow_up=true로 응답해도 질문 본문이 비어 오면(NOT NULL 위배 방지)
    // 예외 대신 꼬리질문 없음으로 우아하게 처리한다.
    if (!aiResponse.isHasFollowUp()
        || aiResponse.getFollowUpQuestion() == null
        || aiResponse.getFollowUpQuestion().isBlank()) {
      return AnswerSubmitResponse.noFollowUp();
    }

    InterviewQuestion followUpQuestion = transactionService.saveFollowUpQuestion(
        request.getQuestionId(), aiResponse.getFollowUpQuestion(), aiResponse.getAudioUrl()
    );

    return AnswerSubmitResponse.followUp(
        followUpQuestion.getId(), followUpQuestion.getQuestionText(), followUpQuestion.getAudioUrl());
  }
}
