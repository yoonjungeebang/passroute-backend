package passroutebackend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.interview.entity.InterviewQuestion;
import passroutebackend.interview.entity.InterviewSession;

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
}