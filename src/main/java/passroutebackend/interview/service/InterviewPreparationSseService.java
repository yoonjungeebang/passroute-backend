package passroutebackend.interview.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import passroutebackend.interview.dto.response.PersonaVideoResponse;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.entity.SessionStatus;
import passroutebackend.interview.repository.InterviewSessionRepository;

@Slf4j
@Service
public class InterviewPreparationSseService {

  private static final long SSE_TIMEOUT = 150_000L;
  private static final long HEARTBEAT_INTERVAL_SECONDS = 30;

  private final InterviewSessionRepository sessionRepository;
  private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
  private final ScheduledExecutorService heartbeatScheduler =
      Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "sse-heartbeat");
        t.setDaemon(true);
        return t;
      });
  private final Map<Long, ScheduledFuture<?>> heartbeats = new ConcurrentHashMap<>();

  public InterviewPreparationSseService(InterviewSessionRepository sessionRepository) {
    this.sessionRepository = sessionRepository;
  }

  public SseEmitter subscribe(Long sessionId, Long userId) {
    SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

    SseEmitter old = emitters.put(sessionId, emitter);
    if (old != null) {
      old.complete();
    }

    emitter.onCompletion(() -> cleanup(sessionId, emitter));
    emitter.onTimeout(() -> cleanup(sessionId, emitter));
    emitter.onError(e -> cleanup(sessionId, emitter));

    startHeartbeat(sessionId, emitter);
    sendCurrentStatus(sessionId, emitter);

    return emitter;
  }

  public void sendReady(Long sessionId, PersonaVideoResponse interviewer) {
    SseEmitter emitter = emitters.get(sessionId);
    if (emitter == null) {
      return;
    }
    try {
      emitter.send(SseEmitter.event()
          .name("preparation-ready")
          .data(Map.of(
              "sessionId", sessionId,
              "status", "IN_PROGRESS",
              "interviewer", interviewer
          )));
      emitter.complete();
    } catch (IOException e) {
      log.warn("SSE 준비완료 전송 실패: sessionId={}", sessionId, e);
    }
  }

  public void sendFailed(Long sessionId, String reason) {
    SseEmitter emitter = emitters.get(sessionId);
    if (emitter == null) {
      return;
    }
    try {
      emitter.send(SseEmitter.event()
          .name("preparation-failed")
          .data(Map.of(
              "sessionId", sessionId,
              "status", "PREPARE_FAILED",
              "reason", reason
          )));
      emitter.complete();
    } catch (IOException e) {
      log.warn("SSE 실패 전송 실패: sessionId={}", sessionId, e);
    }
  }

  private void sendCurrentStatus(Long sessionId, SseEmitter emitter) {
    try {
      InterviewSession session = sessionRepository.findById(sessionId).orElse(null);
      if (session == null) {
        emitter.complete();
        return;
      }

      SessionStatus status = session.getStatus();
      emitter.send(SseEmitter.event()
          .name("preparation-status")
          .data(Map.of("sessionId", sessionId, "status", status.name())));

      if (status == SessionStatus.IN_PROGRESS || status == SessionStatus.PREPARE_FAILED) {
        emitter.complete();
      }
    } catch (IOException e) {
      log.error("SSE 초기 상태 전송 실패: sessionId={}", sessionId, e);
      emitter.completeWithError(e);
    }
  }

  private void startHeartbeat(Long sessionId, SseEmitter emitter) {
    ScheduledFuture<?> future = heartbeatScheduler.scheduleAtFixedRate(() -> {
      try {
        emitter.send(SseEmitter.event().comment("heartbeat"));
      } catch (IOException e) {
        cleanup(sessionId, emitter);
      }
    }, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
    heartbeats.put(sessionId, future);
  }

  private void cleanup(Long sessionId, SseEmitter emitter) {
    emitters.remove(sessionId, emitter);
    ScheduledFuture<?> future = heartbeats.remove(sessionId);
    if (future != null) {
      future.cancel(false);
    }
  }
}
