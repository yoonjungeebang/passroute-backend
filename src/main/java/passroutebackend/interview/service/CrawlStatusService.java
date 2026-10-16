package passroutebackend.interview.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import passroutebackend.interview.dto.response.CrawlStatusResponseDto;
import passroutebackend.interview.entity.CrawlRequestStatus;
import passroutebackend.interview.entity.RoomCrawlRequest;
import passroutebackend.interview.repository.RoomCrawlRequestRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlStatusService {

    private static final long SSE_TIMEOUT = 5 * 60 * 1000L;

    private final RoomCrawlRequestRepository roomCrawlRequestRepository;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long roomId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.put(roomId, emitter);
        emitter.onCompletion(() -> emitters.remove(roomId));
        emitter.onTimeout(() -> emitters.remove(roomId));
        emitter.onError(e -> emitters.remove(roomId));

        sendCurrentStatus(roomId, emitter);
        return emitter;
    }

    @Transactional
    public void handleCrawlEvent(Long roomId, CrawlRequestStatus status, String resultPayload) {
        RoomCrawlRequest request = roomCrawlRequestRepository.findByRoomId(roomId).orElse(null);
        if (request == null) {
            log.warn("크롤링 요청을 찾을 수 없음: roomId={}", roomId);
            return;
        }

        if (isTerminal(request.getStatus())) {
            log.debug("이미 최종 상태: roomId={}, current={}", roomId, request.getStatus());
            return;
        }

        switch (status) {
            case COMPLETED -> request.markCompleted(resultPayload);
            case FAILED -> request.markFailed();
            case NO_DATA -> request.markNoData();
            default -> request.updateStatus(status);
        }
        roomCrawlRequestRepository.save(request);

        SseEmitter emitter = emitters.get(roomId);
        if (emitter != null) {
            sendEvent(emitter, roomId, new CrawlStatusResponseDto(status.name()));
            if (isTerminal(status)) {
                emitter.complete();
                emitters.remove(roomId);
            }
        }
    }

    private void sendCurrentStatus(Long roomId, SseEmitter emitter) {
        try {
            RoomCrawlRequest request = roomCrawlRequestRepository.findByRoomId(roomId)
                    .orElse(null);
            if (request == null) {
                return;
            }
            CrawlRequestStatus status = request.getStatus();
            emitter.send(SseEmitter.event()
                    .name("crawl-status")
                    .data(new CrawlStatusResponseDto(status.name())));
            if (isTerminal(status)) {
                emitter.complete();
                emitters.remove(roomId);
            }
        } catch (Exception e) {
            log.error("SSE 초기 상태 전송 실패: roomId={}", roomId, e);
            emitter.completeWithError(e);
            emitters.remove(roomId);
        }
    }

    private void sendEvent(SseEmitter emitter, Long roomId, CrawlStatusResponseDto data) {
        try {
            emitter.send(SseEmitter.event().name("crawl-status").data(data));
        } catch (Exception e) {
            log.error("SSE 이벤트 전송 실패: roomId={}", roomId, e);
            emitters.remove(roomId);
        }
    }

    private boolean isTerminal(CrawlRequestStatus status) {
        return status == CrawlRequestStatus.COMPLETED
                || status == CrawlRequestStatus.FAILED
                || status == CrawlRequestStatus.NO_DATA;
    }
}
