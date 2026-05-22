package passroutebackend.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import passroutebackend.user.entity.JobType;

import java.util.Set;

@Getter
@NoArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
    private String name;

    @NotBlank(message = "휴대폰번호는 필수입니다.")
    @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "휴대폰번호 형식이 올바르지 않습니다.")
    private String phone;

    @Min(value = 0, message = "연차는 0 이상이어야 합니다.")
    @Max(value = 50, message = "연차는 50 이하여야 합니다.")
    private Integer experienceYears;

    @Size(max = 5, message = "선호 직군은 최대 5개까지 선택 가능합니다.")
    private Set<JobType> preferredJobTypes;

    @Size(max = 10, message = "선호 회사는 최대 10개까지 선택 가능합니다.")
    private Set<String> preferredCompanies;
}
