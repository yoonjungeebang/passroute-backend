package passroutebackend.interview.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.dto.FollowUpRequest;
import passroutebackend.interview.dto.FollowUpResponse;
import passroutebackend.interview.dto.debate.DebateClosingRequest;
import passroutebackend.interview.dto.debate.DebateClosingResponse;
import passroutebackend.interview.dto.debate.DebateOpeningRequest;
import passroutebackend.interview.dto.debate.DebateOpeningResponse;
import passroutebackend.interview.dto.debate.DebateRebuttalRequest;
import passroutebackend.interview.dto.debate.DebateRebuttalResponse;
import passroutebackend.interview.dto.debate.DebateReportRequest;
import passroutebackend.interview.dto.debate.DebateReportResponse;
import passroutebackend.interview.dto.debate.DebateSessionSummaryRequest;
import passroutebackend.interview.dto.debate.DebateSessionSummaryResponse;
import passroutebackend.interview.dto.debate.DebateTurnEvalRequest;
import passroutebackend.interview.dto.debate.DebateTurnEvalResponse;
import passroutebackend.interview.dto.debate.InterviewerClosingRequest;
import passroutebackend.interview.dto.debate.InterviewerClosingResponse;
import passroutebackend.interview.dto.debate.InterviewerOpeningRequest;
import passroutebackend.interview.dto.debate.InterviewerOpeningResponse;
import passroutebackend.interview.dto.debate.TopicDetailAiRequest;
import passroutebackend.interview.dto.debate.TopicDetailAiResponse;
import passroutebackend.interview.dto.debate.TopicSuggestAiRequest;
import passroutebackend.interview.dto.debate.TopicSuggestAiResponse;
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
import passroutebackend.interview.dto.voice.VoiceAnalysisResponse;

@Slf4j
@Component
public class AiServerClient {

  private final RestClient aiServerRestClient;
  private final RestClient debateAiServerRestClient;

  public AiServerClient(
      RestClient aiServerRestClient,
      @Qualifier("debateAiServerRestClient") RestClient debateAiServerRestClient) {
    this.aiServerRestClient = aiServerRestClient;
    this.debateAiServerRestClient = debateAiServerRestClient;
  }

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

  // 음성 분석 통계 조회 (실패 시 null 반환 → 리포트 본문 흐름 격리)
  public VoiceAnalysisResponse getVoiceAnalysis(Long sessionId) {
    try {
      return aiServerRestClient.post()
          .uri("/interview/{sessionId}/end", sessionId)
          .retrieve()
          .body(VoiceAnalysisResponse.class);
    } catch (RestClientResponseException e) {
      log.warn("AI 서버 음성 분석 통계 조회 실패, sessionId={}, status={}, body={}",
          sessionId, e.getStatusCode(), e.getResponseBodyAsString());
      return null;
    } catch (Exception e) {
      log.warn("AI 서버 음성 분석 통계 조회 실패, sessionId={}, error={}", sessionId, e.getMessage());
      return null;
    }
  }

  public ReportGenerationResponse generateReport(ReportGenerationRequest request) {
    return aiServerRestClient.post()
        .uri("/report/generate")
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .body(ReportGenerationResponse.class);
  }

  // ── 토론 면접 (debateAiServerRestClient 사용, timeout 180s) ─────────────────

  public InterviewerOpeningResponse generateInterviewerOpening(InterviewerOpeningRequest request) {
    return callDebateApi("/debate/interviewer-opening", request, InterviewerOpeningResponse.class,
        "면접관 오프닝 생성");
  }

  public DebateOpeningResponse generateDebateOpening(DebateOpeningRequest request) {
    return callDebateApi("/debate/opening", request, DebateOpeningResponse.class,
        "AI 경쟁자 입론 생성");
  }

  public DebateRebuttalResponse generateDebateRebuttal(DebateRebuttalRequest request) {
    return callDebateApi("/debate/rebuttal", request, DebateRebuttalResponse.class,
        "AI 경쟁자 반박 생성");
  }

  public DebateClosingResponse generateDebateClosing(DebateClosingRequest request) {
    return callDebateApi("/debate/closing", request, DebateClosingResponse.class,
        "AI 경쟁자 마무리 생성");
  }

  public InterviewerClosingResponse generateInterviewerClosing(InterviewerClosingRequest request) {
    return callDebateApi("/debate/interviewer-closing", request, InterviewerClosingResponse.class,
        "면접관 마무리 생성");
  }

  public DebateTurnEvalResponse evaluateDebateTurn(DebateTurnEvalRequest request) {
    return callDebateApi("/evaluate/debate-turn", request, DebateTurnEvalResponse.class,
        "토론 사용자 턴 평가");
  }

  public DebateSessionSummaryResponse generateDebateSessionSummary(DebateSessionSummaryRequest request) {
    return callDebateApi("/debate/session-summary", request, DebateSessionSummaryResponse.class,
        "토론 세션 요약");
  }

  public DebateReportResponse generateDebateReport(DebateReportRequest request) {
    return callDebateApi("/report/debate/generate", request, DebateReportResponse.class,
        "토론 리포트 생성");
  }

  // 크롤링 뉴스 기반 토론 주제 추천/생성
  public TopicSuggestAiResponse suggestDebateTopics(TopicSuggestAiRequest request) {
    return callDebateApi("/debate/topics/suggest", request, TopicSuggestAiResponse.class,
        "토론 주제 후보 추천");
  }

  public TopicDetailAiResponse generateDebateTopicDetail(TopicDetailAiRequest request) {
    return callDebateApi("/debate/topics/detail", request, TopicDetailAiResponse.class,
        "토론 주제 상세 생성");
  }

  private <T, R> R callDebateApi(String uri, T request, Class<R> responseType, String actionName) {
    try {
      R response = debateAiServerRestClient.post()
          .uri(uri)
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .body(responseType);
      if (response == null) {
        log.error("AI 서버 {} 응답이 null, uri={}", actionName, uri);
        throw CustomException.of(ErrorCode.AI_SERVER_ERROR);
      }
      return response;
    } catch (CustomException e) {
      throw e;
    } catch (RestClientResponseException e) {
      log.error("AI 서버 {} 실패, uri={}, status={}, body={}",
          actionName, uri, e.getStatusCode(), e.getResponseBodyAsString());
      throw CustomException.of(ErrorCode.AI_SERVER_ERROR);
    } catch (Exception e) {
      log.error("AI 서버 {} 실패, uri={}, error={}", actionName, uri, e.getMessage());
      throw CustomException.of(ErrorCode.AI_SERVER_ERROR);
    }
  }
}
