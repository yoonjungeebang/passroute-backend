package passroutebackend.debate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.debate.entity.DebateSession;

import java.util.Optional;

public interface DebateSessionRepository extends JpaRepository<DebateSession, Long> {

  // 소유권 검증용 (사용자가 자기 세션에만 접근하도록)
  Optional<DebateSession> findByIdAndUserId(Long id, Long userId);
}
