package passroutebackend.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "crawler.websocket")
public class CrawlerProperties {

    private String endpoint;
    private String apiKey;
    private int timeoutSeconds = 120;
    private int maxArticles = 10;
}
