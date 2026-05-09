package passroutebackend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.interview.entity.InterviewSession;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
}
