package passroutebackend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewSession;

import java.util.List;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    List<InterviewSession> findByInterviewRoomOrderBySessionNumberAsc(InterviewRoom interviewRoom);

    int countByInterviewRoom(InterviewRoom interviewRoom);
}
