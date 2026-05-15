package passroutebackend.global.config;

import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import passroutebackend.global.property.FollowUpProperties;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

  private final FollowUpProperties followUpProperties;

  @Bean
  public RestClient aiServerRestClient() {
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setMaxTotal(10);
    connectionManager.setDefaultMaxPerRoute(5);

    Timeout timeout = Timeout.ofSeconds(followUpProperties.getTimeoutSeconds());
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectionRequestTimeout(timeout)
        .setResponseTimeout(timeout)
        .build();

    CloseableHttpClient httpClient = HttpClients.custom()
        .setConnectionManager(connectionManager)
        .setDefaultRequestConfig(requestConfig)
        .build();

    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

    return RestClient.builder()
        .baseUrl(followUpProperties.getAiServerUrl())
        .requestFactory(factory)
        .build();
  }
}
