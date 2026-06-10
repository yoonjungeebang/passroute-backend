package passroutebackend.interview.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import passroutebackend.interview.entity.InterviewReport;
import passroutebackend.interview.entity.InterviewSession;

import java.util.List;
import java.util.Optional;

public interface InterviewReportRepository extends JpaRepository<InterviewReport, Long> {

  Optional<InterviewReport> findBySession(InterviewSession session);

  // 워커 완료/실패 갱신 시 행 잠금 → 워치독과의 동시 전이 직렬화
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT r FROM InterviewReport r WHERE r.session = :session")
  Optional<InterviewReport> findBySessionForUpdate(@Param("session") InterviewSession session);

  // 워치독: GENERATING일 때만 FAILED로 조건부 전환 (COMPLETED 덮어쓰기/부활 방지). 갱신 행 수 반환
  @Modifying(clearAutomatically = true)
  @Query("""
      UPDATE InterviewReport r
        SET r.reportStatus = passroutebackend.interview.entity.ReportStatus.FAILED
      WHERE r.id = :id
        AND r.reportStatus = passroutebackend.interview.entity.ReportStatus.GENERATING
      """)
  int markStaleAsFailed(@Param("id") Long id);

  @Query("""
      SELECT r FROM InterviewReport r
        JOIN FETCH r.session s
        JOIN FETCH s.interviewRoom rm
      WHERE rm.siId = :siId
        AND s.isActive = true
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
        AND rm.companyName IN :companyNames
        AND s.isActive = true
        AND s.status = passroutebackend.interview.entity.SessionStatus.COMPLETED
        AND r.reportStatus = passroutebackend.interview.entity.ReportStatus.COMPLETED
      ORDER BY s.endedAt DESC
      """)
  List<InterviewReport> findCompletedByUserIdAndCompanyNames(
      @Param("userId") Long userId, @Param("companyNames") List<String> companyNames);
}
