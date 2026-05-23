package passroutebackend.global.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class S3TestConfig {

    @Bean
    public S3Client s3Client() {
        return mock(S3Client.class);
    }

    @Bean
    public S3Presigner s3Presigner() {
        return mock(S3Presigner.class);
    }
}
