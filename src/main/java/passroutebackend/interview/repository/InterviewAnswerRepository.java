package passroutebackend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.interview.entity.InterviewAnswer;
import passroutebackend.interview.entity.InterviewQuestion;
import passroutebackend.interview.entity.InterviewSession;

import java.util.List;
import java.util.Optional;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {

  Optional<InterviewAnswer> findByQuestion(InterviewQuestion question);

  List<InterviewAnswer> findByQuestionIn(List<InterviewQuestion> questions);

  Optional<InterviewAnswer> findFirstByQuestionSessionAndClipScoreIsNotNullOrderByClipScoreAsc(InterviewSession session);

  Optional<InterviewAnswer> findByQuestion_Id(Long questionId);
}
