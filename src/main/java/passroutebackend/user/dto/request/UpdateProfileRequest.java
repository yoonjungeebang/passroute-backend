package passroutebackend.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import passroutebackend.user.entity.JobType;

import java.util.Set;

@Schema(description = "프로필 수정 요청")
@Getter
public class UpdateProfileRequest {

    @Schema(description = "경력 연수", example = "3")
    @Min(value = 0, message = "경력 연수는 0 이상이어야 합니다.")
    private Integer experienceYears;

    @Schema(description = "선호 직군")
    private Set<JobType> preferredJobTypes;

    @Schema(description = "선호 회사")
    private Set<String> preferredCompanies;
}
