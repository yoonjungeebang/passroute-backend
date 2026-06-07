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

  Optional<InterviewAnswer> findFirstByQuestionSessionAndClipScoreIsNotNullOrderByClipScoreAsc(InterviewSession session);

  Optional<InterviewAnswer> findByQuestion_Id(Long questionId);
  // 리포트 생성 전 평가 완료 대기용 진행도
  @Query("SELECT COUNT(a) FROM InterviewAnswer a WHERE a.question.session.id = :sessionId")
  long countBySessionId(@Param("sessionId") Long sessionId);

  @Query("SELECT COUNT(a) FROM InterviewAnswer a WHERE a.question.session.id = :sessionId AND a.percentage IS NOT NULL")
  long countEvaluatedBySessionId(@Param("sessionId") Long sessionId);
}
