package passroutebackend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.interview.entity.InterviewRoom;

public interface InterviewRoomRepository extends JpaRepository<InterviewRoom, Long> {
}
