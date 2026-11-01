package passroutebackend.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Schema(description = "비밀번호 재설정 요청")
@Getter
public class PasswordResetRequest {

    @Schema(description = "휴대폰번호", example = "01012345678")
    @NotBlank
    private String phone;

    @Schema(description = "인증번호", example = "123456")
    @NotBlank
    private String code;

    @Schema(description = "새 비밀번호 (8자 이상)", example = "newPassword123")
    @NotBlank
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String newPassword;
}
