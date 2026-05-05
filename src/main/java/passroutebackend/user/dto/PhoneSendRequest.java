package passroutebackend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PhoneSendRequest {

    @NotBlank(message = "휴대폰번호는 필수입니다.")
    @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "휴대폰번호 형식이 올바르지 않습니다.")
    private String phone;
}
