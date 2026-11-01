package passroutebackend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.entity.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    List<InterviewSession> findByInterviewRoomAndIsActiveTrueOrderBySessionNumberAsc(InterviewRoom interviewRoom);

    int countByInterviewRoom(InterviewRoom interviewRoom);

    Optional<InterviewSession> findByInterviewRoomAndStatusIn(
        InterviewRoom room, List<SessionStatus> statuses);

    @Query("SELECT s FROM InterviewSession s WHERE s.status = :status AND s.startedAt < :cutoff")
    List<InterviewSession> findExpiredSessions(
        @Param("status") SessionStatus status,
        @Param("cutoff") LocalDateTime cutoff);
}
