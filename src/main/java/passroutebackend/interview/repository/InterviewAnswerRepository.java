package passroutebackend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.interview.entity.InterviewAnswer;
import passroutebackend.interview.entity.InterviewQuestion;

import java.util.Optional;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {

  Optional<InterviewAnswer> findByQuestion(InterviewQuestion question);
}
