package passroutebackend.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "회원 탈퇴 요청")
@Getter
public class DeleteAccountRequest {

    @Schema(description = "현재 비밀번호")
    private String password;
}
