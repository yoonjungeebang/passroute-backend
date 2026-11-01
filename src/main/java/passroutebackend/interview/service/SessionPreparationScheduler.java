package passroutebackend.interview.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.property.InterviewPreparationProperties;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.entity.SessionStatus;
import passroutebackend.interview.repository.InterviewSessionRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionPreparationScheduler {

  private final InterviewSessionRepository sessionRepository;
  private final InterviewPreparationProperties properties;
  private final InterviewPreparationSseService sseService;

  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  public void cleanupOnStartup() {
    List<InterviewSession> stale = sessionRepository
        .findExpiredSessions(SessionStatus.PREPARING, LocalDateTime.now());
    for (InterviewSession session : stale) {
      session.markPrepareFailed();
      log.info("시작 시 잔여 PREPARING 세션 실패 처리: sessionId={}", session.getId());
    }
  }

  @Scheduled(fixedDelayString = "${interview.preparation.cleanup-interval-seconds:60}000")
  @Transactional
  public void expireStalePreparingSessions() {
    LocalDateTime cutoff = LocalDateTime.now().minusMinutes(properties.getTimeoutMinutes());
    List<InterviewSession> expired = sessionRepository
        .findExpiredSessions(SessionStatus.PREPARING, cutoff);

    for (InterviewSession session : expired) {
      if (session.markPrepareFailed()) {
        log.info("만료된 PREPARING 세션 실패 처리: sessionId={}", session.getId());
        sseService.sendFailed(session.getId(), "질문 생성 시간이 초과되었습니다.");
      }
    }
  }
}
