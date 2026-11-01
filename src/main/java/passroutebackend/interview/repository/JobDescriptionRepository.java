package passroutebackend.interview.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.interview.entity.JobDescription;

public interface JobDescriptionRepository extends JpaRepository<JobDescription, String> {

    List<JobDescription> findByCompanyNameNormAndSourceIn(String companyNameNorm, List<String> sources);
}
