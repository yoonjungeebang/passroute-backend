package passroutebackend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import passroutebackend.interview.entity.InterviewReport;
import passroutebackend.interview.entity.InterviewSession;

import java.util.List;
import java.util.Optional;

public interface InterviewReportRepository extends JpaRepository<InterviewReport, Long> {

  Optional<InterviewReport> findBySession(InterviewSession session);

  @Query("""
      SELECT r FROM InterviewReport r
        JOIN FETCH r.session s
        JOIN FETCH s.interviewRoom rm
      WHERE rm.siId = :siId
        AND s.status = passroutebackend.interview.entity.SessionStatus.COMPLETED
        AND r.reportStatus = passroutebackend.interview.entity.ReportStatus.COMPLETED
      ORDER BY s.endedAt ASC
      """)
  List<InterviewReport> findAllBySiIdOrderByEndedAtAsc(@Param("siId") Long siId);

  @Query("""
      SELECT r FROM InterviewReport r
        JOIN FETCH r.session s
        JOIN FETCH s.interviewRoom rm
      WHERE rm.userId = :userId
        AND rm.companyName = :companyName
        AND s.status = passroutebackend.interview.entity.SessionStatus.COMPLETED
        AND r.reportStatus = passroutebackend.interview.entity.ReportStatus.COMPLETED
      ORDER BY s.endedAt DESC
      """)
  List<InterviewReport> findCompletedByUserIdAndCompanyName(
      @Param("userId") Long userId, @Param("companyName") String companyName);
}
