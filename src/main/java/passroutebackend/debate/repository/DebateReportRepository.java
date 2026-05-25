package passroutebackend.debate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.debate.entity.DebateReport;
import passroutebackend.debate.entity.DebateSession;

import java.util.Optional;

public interface DebateReportRepository extends JpaRepository<DebateReport, Long> {

  // 세션별 리포트 조회 (GET /debate/{sessionId}/report)
  Optional<DebateReport> findBySession(DebateSession session);
}
