package passroutebackend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.interview.entity.InterviewReport;
import passroutebackend.interview.entity.InterviewSession;

import java.util.Optional;

public interface InterviewReportRepository extends JpaRepository<InterviewReport, Long> {

  Optional<InterviewReport> findBySession(InterviewSession session);
}
