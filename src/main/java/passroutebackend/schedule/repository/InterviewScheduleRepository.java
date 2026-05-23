package passroutebackend.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.schedule.entity.InterviewSchedule;

import java.time.LocalDateTime;
import java.util.List;

public interface InterviewScheduleRepository extends JpaRepository<InterviewSchedule, Long> {

    List<InterviewSchedule> findByUserIdOrderByInterviewDateAsc(Long userId);

    List<InterviewSchedule> findByUserIdAndInterviewDateBetweenOrderByInterviewDateAsc(
            Long userId, LocalDateTime start, LocalDateTime end);
}
