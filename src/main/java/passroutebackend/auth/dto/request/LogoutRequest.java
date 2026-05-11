package passroutebackend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
@Getter
public class LogoutRequest {
    @NotBlank
    private String refreshToken;
}
