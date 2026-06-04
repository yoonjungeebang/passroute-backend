package passroutebackend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.interview.entity.VoiceAnalysis;

import java.util.List;

public interface VoiceAnalysisRepository extends JpaRepository<VoiceAnalysis, Long> {

  List<VoiceAnalysis> findBySessionId(Long sessionId);
}
