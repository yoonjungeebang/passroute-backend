package passroutebackend.auth.controller;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.ServerSocket;

@TestConfiguration
public class EmbeddedRedisConfig {

    private static final int REDIS_PORT = 6379;

    private RedisServer redisServer;

    @PostConstruct
    public void start() throws IOException {
        if (isPortInUse(REDIS_PORT)) {
            return;
        }
        redisServer = new RedisServer(REDIS_PORT);
        redisServer.start();
    }

    @PreDestroy
    public void stop() throws IOException {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    private boolean isPortInUse(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }
}
