package passroutebackend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.interview.entity.InterviewQuestion;
import passroutebackend.interview.entity.InterviewSession;

import java.util.List;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

  int countBySessionAndSetNumberAndFollowUpTrue(InterviewSession session, int setNumber);

  List<InterviewQuestion> findBySessionAndSetNumberOrderByQuestionOrderAsc(
      InterviewSession session, int setNumber);

  List<InterviewQuestion> findBySession(InterviewSession session);
}
