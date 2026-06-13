package passroutebackend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import passroutebackend.interview.entity.InterviewFormat;
import passroutebackend.interview.entity.InterviewQuestion;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.entity.InterviewType;

import java.util.List;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

  // FollowUpTransactionService: 세트 단위 질문 조회
  List<InterviewQuestion> findBySessionAndSetNumberOrderByQuestionOrderAsc(
      InterviewSession session, int setNumber);

  // InterviewHistoryService: 여러 세션의 질문 일괄 조회
  List<InterviewQuestion> findBySessionInOrderBySessionIdAscQuestionOrderAsc(
      List<InterviewSession> sessions);

  // ReportTransactionService + InterviewSessionTxService: 세션 전체 질문 조회
  List<InterviewQuestion> findBySessionOrderByQuestionOrderAsc(InterviewSession session);

  // 세트 순서 → 세트 내 순서로 정렬 (saveQuestions 픽스 이후 생성된 세션용)
  List<InterviewQuestion> findBySessionOrderBySetNumberAscQuestionOrderAsc(InterviewSession session);

  // CsTopicAnalysisService: 사용자의 1:1 기술면접 질문을 토픽별로 집계
  @Query("SELECT q.csTopic AS csTopic, COUNT(q) AS questionCount, AVG(a.percentage) AS avgPercentage "
      + "FROM InterviewQuestion q JOIN InterviewAnswer a ON a.question = q "
      + "WHERE q.session.interviewRoom.userId = :userId "
      + "AND q.session.interviewRoom.interviewType = :interviewType "
      + "AND q.session.interviewRoom.interviewFormat = :interviewFormat "
      + "AND q.csTopic IS NOT NULL "
      + "AND a.percentage IS NOT NULL "
      + "GROUP BY q.csTopic")
  List<CsTopicStatProjection> aggregateCsTopicStats(
      @Param("userId") Long userId,
      @Param("interviewType") InterviewType interviewType,
      @Param("interviewFormat") InterviewFormat interviewFormat);
}