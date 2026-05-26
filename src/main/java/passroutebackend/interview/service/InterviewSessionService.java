package passroutebackend.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import passroutebackend.interview.dto.AnswerSubmitRequest;
import passroutebackend.interview.dto.AnswerSubmitResponse;
import passroutebackend.interview.dto.response.AnswerProgressResponse;
import passroutebackend.interview.dto.response.SessionQuestionListResponse;

@Service
@RequiredArgsConstructor
public class InterviewSessionService {

  private final InterviewSessionTxService txService;
  private final FollowUpService followUpService;

  public SessionQuestionListResponse getQuestions(Long sessionId, Long userId) {
    return txService.getQuestions(sessionId, userId);
  }

  public AnswerProgressResponse submitAnswer(Long sessionId, Long userId, AnswerSubmitRequest request) {
    // 세션 유효성 검증 (소유자 확인 + 진행 중 상태 확인)
    txService.validateSessionForAnswer(sessionId, userId);

    // 답변 저장 + 꼬리질문 생성 + 비동기 평가 (기존 서비스 재사용)
    AnswerSubmitResponse followUpResponse = followUpService.submitAnswer(request);

    // 마지막 질문 여부 계산 (답변 저장 완료 후 조회)
    boolean isLastQuestion = txService.computeIsLastQuestion(sessionId, followUpResponse.isHasFollowUp());

    return AnswerProgressResponse.of(
        followUpResponse.isHasFollowUp(),
        followUpResponse.getFollowUpQuestionId(),
        followUpResponse.getFollowUpQuestionText(),
        isLastQuestion
    );
  }

  public void endSession(Long sessionId, Long userId) {
    txService.endSession(sessionId, userId);
  }
}
