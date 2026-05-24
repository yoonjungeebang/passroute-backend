package passroutebackend.interview.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.dto.FollowUpRequest;
import passroutebackend.interview.dto.FollowUpResponse;
import passroutebackend.interview.dto.evaluation.QuestionEvaluationRequest;
import passroutebackend.interview.dto.evaluation.QuestionEvaluationResponse;
import passroutebackend.interview.dto.evaluation.StarEvaluationRequest;
import passroutebackend.interview.dto.evaluation.StarEvaluationResponse;
import passroutebackend.interview.dto.generate.QuestionGenerateRequest;
import passroutebackend.interview.dto.generate.QuestionGenerateResponse;
import passroutebackend.interview.dto.report.ReportGenerationRequest;
import passroutebackend.interview.dto.report.ReportGenerationResponse;
import passroutebackend.interview.dto.report.SessionSummaryRequest;
import passroutebackend.interview.dto.report.SessionSummaryResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiServerClient {

  private final RestClient aiServerRestClient;

  public QuestionGenerateResponse generateQuestions(QuestionGenerateRequest request) {
    try {
      QuestionGenerateResponse response = aiServerRestClient.post()
          .uri("/api/questions/generate")
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .body(QuestionGenerateResponse.class);

      if (response == null || response.getQuestions() == null || response.getQuestions().isEmpty()) {
        throw CustomException.of(ErrorCode.AI_SERVER_ERROR);
      }
      return response;
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("AI 서버 질문 생성 실패: {}", e.getMessage());
      throw CustomException.of(ErrorCode.AI_SERVER_ERROR);
    }
  }

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

  public SessionSummaryResponse sessionSummary(SessionSummaryRequest request) {
    return aiServerRestClient.post()
        .uri("/evaluate/session-summary")
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .body(SessionSummaryResponse.class);
  }

  public ReportGenerationResponse generateReport(ReportGenerationRequest request) {
    return aiServerRestClient.post()
        .uri("/report/generate")
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .body(ReportGenerationResponse.class);
  }
}
