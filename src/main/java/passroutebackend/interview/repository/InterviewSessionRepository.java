package passroutebackend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewSession;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

  int countByInterviewRoom(InterviewRoom interviewRoom);
}
