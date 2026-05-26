package passroutebackend.user.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import passroutebackend.user.entity.JobType;

import java.util.Set;

@Getter
public class UpdateProfileRequest {

    @Min(value = 0, message = "경력 연수는 0 이상이어야 합니다.")
    private Integer experienceYears;

    private Set<JobType> preferredJobTypes;

    private Set<String> preferredCompanies;
}
