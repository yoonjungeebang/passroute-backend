package passroutebackend.debate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.debate.entity.AiPersona;

import java.util.Optional;

public interface AiPersonaRepository extends JpaRepository<AiPersona, Long> {

  // 시드 로더 멱등 적재용
  Optional<AiPersona> findByPersonaKey(String personaKey);
}
