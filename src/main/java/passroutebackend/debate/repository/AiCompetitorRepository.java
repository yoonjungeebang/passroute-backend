package passroutebackend.debate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.debate.entity.AiCompetitor;

public interface AiCompetitorRepository extends JpaRepository<AiCompetitor, Long> {
}
