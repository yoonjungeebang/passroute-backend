package passroutebackend.user.dto.request;

import jakarta.validation.constraints.Min;
import passroutebackend.user.entity.JobType;

import java.util.Set;

public record UpdateProfileRequest(
        @Min(value = 0, message = "경력 연수는 0 이상이어야 합니다.")
        Integer experienceYears,
        Set<JobType> preferredJobTypes,
        Set<String> preferredCompanies
) {}
