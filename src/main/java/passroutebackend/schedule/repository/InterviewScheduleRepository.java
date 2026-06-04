package passroutebackend.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.schedule.entity.InterviewSchedule;
import passroutebackend.schedule.entity.ScheduleStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InterviewScheduleRepository extends JpaRepository<InterviewSchedule, Long> {

    List<InterviewSchedule> findByUserIdOrderByInterviewDateAsc(Long userId);

    List<InterviewSchedule> findByUserIdAndInterviewDateBetweenOrderByInterviewDateAsc(
            Long userId, LocalDateTime start, LocalDateTime end);

    Optional<InterviewSchedule> findBySelfIntroIdAndStatus(Long selfIntroId, ScheduleStatus status);

    boolean existsBySelfIntroIdAndStatus(Long selfIntroId, ScheduleStatus status);
}
