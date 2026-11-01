package passroutebackend.interview.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import passroutebackend.global.service.PersonaVideoService;
import passroutebackend.interview.client.AiServerClient;
import passroutebackend.interview.dto.generate.QuestionGenerateResponse;
import passroutebackend.interview.dto.generate.SessionPreparation;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewStartAsyncService {

  private final InterviewStartTxService txService;
  private final AiServerClient aiServerClient;
  private final PersonaVideoService personaVideoService;
  private final InterviewPreparationSseService sseService;

  @Async("interviewStartExecutor")
  public void prepareInBackground(SessionPreparation prep) {
    try {
      QuestionGenerateResponse aiResponse = aiServerClient.generateQuestions(prep.getAiRequest());

      boolean activated = txService.saveQuestionsAndActivate(
          prep.getSessionId(), aiResponse.getQuestions());

      if (activated) {
        sseService.sendReady(prep.getSessionId(), personaVideoService.getInterviewer(prep.getAiInterviewer()));
      } else {
        sseService.sendFailed(prep.getSessionId(), "세션 상태 전이에 실패했습니다.");
      }
    } catch (Exception e) {
      log.error("면접 질문 생성 실패: sessionId={}", prep.getSessionId(), e);
      try {
        txService.markPrepareFailed(prep.getSessionId());
      } catch (Exception ex) {
        log.error("실패 상태 저장 오류: sessionId={}", prep.getSessionId(), ex);
      }
      sseService.sendFailed(prep.getSessionId(), "질문 생성에 실패했습니다.");
    }
  }
}
