package passroutebackend.interview.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import passroutebackend.interview.dto.FollowUpRequest;
import passroutebackend.interview.dto.FollowUpResponse;
import passroutebackend.interview.dto.evaluation.QuestionEvaluationRequest;
import passroutebackend.interview.dto.evaluation.QuestionEvaluationResponse;
import passroutebackend.interview.dto.evaluation.StarEvaluationRequest;
import passroutebackend.interview.dto.evaluation.StarEvaluationResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiServerClient {

  private final RestClient aiServerRestClient;

  public FollowUpResponse requestFollowUp(FollowUpRequest request) {
    try {
      FollowUpResponse response = aiServerRestClient.post()
          .uri("/api/follow-up")
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .body(FollowUpResponse.class);

      return response != null ? response : FollowUpResponse.noFollowUp();
    } catch (Exception e) {
      log.warn("AI 서버 호출 실패, 꼬리질문 없이 진행합니다: {}", e.getMessage());
      return FollowUpResponse.noFollowUp();
    }
  }

  public QuestionEvaluationResponse evaluateQuestion(QuestionEvaluationRequest request) {
    return aiServerRestClient.post()
        .uri("/evaluate/question")
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .body(QuestionEvaluationResponse.class);
  }

  public StarEvaluationResponse evaluateStar(StarEvaluationRequest request) {
    return aiServerRestClient.post()
        .uri("/evaluate/star")
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .body(StarEvaluationResponse.class);
  }
}
