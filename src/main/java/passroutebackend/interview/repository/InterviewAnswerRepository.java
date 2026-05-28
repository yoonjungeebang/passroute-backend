package passroutebackend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import passroutebackend.interview.entity.InterviewAnswer;
import passroutebackend.interview.entity.InterviewQuestion;
import passroutebackend.interview.entity.InterviewSession;

import java.util.List;
import java.util.Optional;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {

  Optional<InterviewAnswer> findByQuestion(InterviewQuestion question);

  List<InterviewAnswer> findByQuestionIn(List<InterviewQuestion> questions);

  @Query("SELECT a FROM InterviewAnswer a WHERE a.question.session = :session AND a.clipScore IS NOT NULL ORDER BY a.clipScore ASC")
  List<InterviewAnswer> findBySessionOrderByClipScoreAsc(@Param("session") InterviewSession session);
}
