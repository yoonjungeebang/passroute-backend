package passroutebackend.interview.service;

import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.dto.generate.SessionPreparation;
import passroutebackend.interview.dto.request.InterviewStartRequest;
import passroutebackend.interview.dto.response.InterviewStartResponse;
import passroutebackend.interview.entity.InterviewSession;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewStartService {

  private final InterviewStartTxService txService;
  private final InterviewStartAsyncService asyncService;

  public InterviewStartResponse start(Long userId, InterviewStartRequest request) {
    Optional<InterviewSession> existing = txService.findActiveSession(request.getRoomId());
    if (existing.isPresent()) {
      InterviewSession session = existing.get();
      return InterviewStartResponse.existing(session.getId(), session.getStatus());
    }

    SessionPreparation prep = txService.createPreparingSession(
        request.getRoomId(), userId);

    try {
      asyncService.prepareInBackground(prep);
    } catch (RejectedExecutionException e) {
      log.warn("면접 시작 스레드풀 포화: sessionId={}", prep.getSessionId());
      txService.markPrepareFailed(prep.getSessionId());
      throw CustomException.of(ErrorCode.INTERVIEW_START_BUSY);
    }

    return InterviewStartResponse.preparing(prep.getSessionId());
  }
}
