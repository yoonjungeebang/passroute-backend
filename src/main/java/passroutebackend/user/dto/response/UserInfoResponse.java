package passroutebackend.user.dto.response;

import passroutebackend.user.entity.JobType;

import java.time.LocalDateTime;
import java.util.Set;

public record UserInfoResponse(
        Long id,
        String email,
        String name,
        String phone,
        String provider,
        boolean emailVerified,
        boolean phoneVerified,
        Integer experienceYears,
        Set<String> preferredCompanies,
        Set<JobType> preferredJobTypes,
        LocalDateTime createdAt
) {}