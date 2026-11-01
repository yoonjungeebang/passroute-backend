package passroutebackend.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Schema(description = "로그아웃 요청")
@Getter
public class LogoutRequest {

    @Schema(description = "리프레시 토큰")
    @NotBlank
    private String refreshToken;
}
