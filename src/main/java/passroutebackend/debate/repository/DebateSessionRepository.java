package passroutebackend.debate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import passroutebackend.debate.entity.DebateSession;

import java.util.Optional;

public interface DebateSessionRepository extends JpaRepository<DebateSession, Long> {

  // 소유권 검증용 (사용자가 자기 세션에만 접근하도록)
  // topic + aiCompetitor + persona까지 fetch join — 비동기 메서드에서 LazyInitializationException 방지
  @Query("SELECT s FROM DebateSession s "
      + "JOIN FETCH s.topic "
      + "LEFT JOIN FETCH s.aiCompetitor c "
      + "LEFT JOIN FETCH c.persona "
      + "WHERE s.id = :id AND s.userId = :userId")
  Optional<DebateSession> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
