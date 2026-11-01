package passroutebackend.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "이메일 찾기 응답")
@Getter
@AllArgsConstructor
public class FindEmailResponse {

    @Schema(description = "마스킹된 이메일", example = "us***@example.com")
    private String maskedEmail;
}
