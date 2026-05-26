package passroutebackend.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.user.entity.JobType;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@AllArgsConstructor
public class UserInfoResponse {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private String provider;
    private boolean emailVerified;
    private boolean phoneVerified;
    private Integer experienceYears;
    private Set<String> preferredCompanies;
    private Set<JobType> preferredJobTypes;
    private LocalDateTime createdAt;
}
