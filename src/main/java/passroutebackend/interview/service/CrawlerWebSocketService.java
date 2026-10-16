package passroutebackend.interview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import passroutebackend.global.config.CrawlerProperties;
import passroutebackend.interview.entity.CrawlRequestStatus;
import passroutebackend.interview.entity.RoomCrawlRequest;
import passroutebackend.interview.repository.RoomCrawlRequestRepository;

@Slf4j
@Profile("!test")
@Service
@RequiredArgsConstructor
public class CrawlerWebSocketService {

    private final CrawlerProperties crawlerProperties;
    private final CrawlStatusService crawlStatusService;
    private final RoomCrawlRequestRepository roomCrawlRequestRepository;
    private final ObjectMapper objectMapper;

    @Async("crawlerExecutor")
    public void triggerCrawl(Long roomId, String companyName, String jobPosition) {
        RoomCrawlRequest crawlRequest = RoomCrawlRequest.builder()
                .roomId(roomId)
                .status(CrawlRequestStatus.PENDING)
                .build();
        roomCrawlRequestRepository.save(crawlRequest);

        CompletableFuture<String> resultFuture = new CompletableFuture<>();
        CrawlerMessageHandler handler = new CrawlerMessageHandler(
                roomId, objectMapper, crawlStatusService, resultFuture);

        WebSocketSession session = null;
        try {
            URI uri = URI.create(crawlerProperties.getEndpoint()
                    + "?x-api-key=" + crawlerProperties.getApiKey());
            StandardWebSocketClient client = new StandardWebSocketClient();
            CompletableFuture<WebSocketSession> sessionFuture =
                    client.execute(handler, new WebSocketHttpHeaders(), uri);

            session = sessionFuture.get(10, TimeUnit.SECONDS);
            String requestBody = buildCollectRequest(companyName, jobPosition);
            session.sendMessage(new TextMessage(requestBody));

            resultFuture.orTimeout(crawlerProperties.getTimeoutSeconds(), TimeUnit.SECONDS)
                    .join();
        } catch (Exception e) {
            log.error("크롤링 실패: roomId={}", roomId, e);
            crawlStatusService.handleCrawlEvent(roomId, CrawlRequestStatus.FAILED, null);
            closeSessionQuietly(session);
        }
    }

    private String buildCollectRequest(String companyName, String jobPosition) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("action", "collect");
        request.put("company", companyName);
        if (jobPosition != null) {
            request.put("position", jobPosition);
        }
        request.put("max_articles", crawlerProperties.getMaxArticles());
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("크롤러 요청 직렬화 실패", e);
        }
    }

    private void closeSessionQuietly(WebSocketSession session) {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            log.debug("WebSocket 세션 종료 중 오류", e);
        }
    }
}
