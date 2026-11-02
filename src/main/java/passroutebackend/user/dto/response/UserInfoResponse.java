package passroutebackend.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.user.entity.JobType;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "사용자 정보 응답")
@Getter
@AllArgsConstructor
public class UserInfoResponse {

    @Schema(description = "사용자 ID")
    private Long id;
    @Schema(description = "이메일")
    private String email;
    @Schema(description = "이름")
    private String name;
    @Schema(description = "휴대폰번호")
    private String phone;
    @Schema(description = "소셜 로그인 제공자")
    private String provider;
    @Schema(description = "이메일 인증 여부")
    private boolean emailVerified;
    @Schema(description = "휴대폰 인증 여부")
    private boolean phoneVerified;
    @Schema(description = "경력 연수")
    private Integer experienceYears;
    @Schema(description = "선호 회사")
    private Set<String> preferredCompanies;
    @Schema(description = "선호 직군")
    private Set<JobType> preferredJobTypes;
    @Schema(description = "가입 일시")
    private LocalDateTime createdAt;
}
