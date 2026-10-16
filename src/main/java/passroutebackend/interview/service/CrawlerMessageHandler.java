package passroutebackend.interview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import passroutebackend.interview.entity.CrawlRequestStatus;

@Slf4j
public class CrawlerMessageHandler extends TextWebSocketHandler {

    private final Long roomId;
    private final ObjectMapper objectMapper;
    private final CrawlStatusService crawlStatusService;
    private final CompletableFuture<String> resultFuture;

    public CrawlerMessageHandler(Long roomId,
                                 ObjectMapper objectMapper,
                                 CrawlStatusService crawlStatusService,
                                 CompletableFuture<String> resultFuture) {
        this.roomId = roomId;
        this.objectMapper = objectMapper;
        this.crawlStatusService = crawlStatusService;
        this.resultFuture = resultFuture;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        JsonNode node = objectMapper.readTree(payload);
        String status = node.path("status").asText();

        switch (status) {
            case "accepted" -> {
                log.info("크롤링 요청 접수: roomId={}", roomId);
                crawlStatusService.handleCrawlEvent(roomId, CrawlRequestStatus.ACCEPTED, null);
            }
            case "partial" -> {
                log.debug("크롤링 수집 중: roomId={}", roomId);
                crawlStatusService.handleCrawlEvent(roomId, CrawlRequestStatus.IN_PROGRESS, null);
            }
            case "cached", "done", "cache_only" -> {
                log.info("크롤링 완료: roomId={}, status={}", roomId, status);
                crawlStatusService.handleCrawlEvent(roomId, CrawlRequestStatus.COMPLETED, payload);
                resultFuture.complete(payload);
                session.close();
            }
            case "no_data" -> {
                log.info("크롤링 데이터 없음: roomId={}", roomId);
                crawlStatusService.handleCrawlEvent(roomId, CrawlRequestStatus.NO_DATA, null);
                resultFuture.complete(null);
                session.close();
            }
            default -> log.warn("알 수 없는 크롤러 상태: roomId={}, status={}", roomId, status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("크롤러 WebSocket 전송 오류: roomId={}", roomId, exception);
        resultFuture.completeExceptionally(exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        log.debug("크롤러 WebSocket 연결 종료: roomId={}, status={}", roomId, closeStatus);
        if (!resultFuture.isDone()) {
            resultFuture.completeExceptionally(
                    new IllegalStateException("WebSocket 연결이 예기치 않게 종료됨: " + closeStatus));
        }
    }
}
