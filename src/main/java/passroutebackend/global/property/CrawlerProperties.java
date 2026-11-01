package passroutebackend.global.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "crawler.websocket")
public class CrawlerProperties {

    private String endpoint;
    private String apiKey;
    private int timeoutSeconds = 120;
    private int maxArticles = 10;
}
