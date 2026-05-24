package passroutebackend.global.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Properties {
    private String redirectUri;
}
