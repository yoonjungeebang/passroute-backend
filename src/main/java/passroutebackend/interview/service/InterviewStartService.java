package passroutebackend.interview.service;

import java.util.Optional;
import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.global.property.InterviewPreparationProperties;
import passroutebackend.interview.dto.generate.SessionPreparation;
import passroutebackend.interview.dto.request.InterviewStartRequest;
import passroutebackend.interview.dto.response.InterviewStartResponse;
import passroutebackend.interview.entity.InterviewSession;

@Slf4j
@Service
public class InterviewStartService {

  private final InterviewStartTxService txService;
  private final InterviewStartAsyncService asyncService;
  private final Semaphore preparationPermits;

  public InterviewStartService(
      InterviewStartTxService txService,
      InterviewStartAsyncService asyncService,
      InterviewPreparationProperties properties) {
    this.txService = txService;
    this.asyncService = asyncService;
    this.preparationPermits = new Semaphore(properties.getMaxConcurrent());
  }

  public InterviewStartResponse start(Long userId, InterviewStartRequest request) {
    Optional<InterviewSession> existing = txService.findActiveSession(request.getRoomId());
    if (existing.isPresent()) {
      InterviewSession session = existing.get();
      return InterviewStartResponse.existing(session.getId(), session.getStatus());
    }

    if (!preparationPermits.tryAcquire()) {
      throw CustomException.of(ErrorCode.INTERVIEW_START_BUSY);
    }

    boolean handedOff = false;
    try {
      SessionPreparation prep = txService.createPreparingSession(
          request.getRoomId(), userId);

      asyncService.prepareInBackground(prep, preparationPermits);
      handedOff = true;

      return InterviewStartResponse.preparing(prep.getSessionId());
    } finally {
      if (!handedOff) {
        preparationPermits.release();
      }
    }
  }
}
