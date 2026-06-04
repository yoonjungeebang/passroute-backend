package passroutebackend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.interview.entity.FaceAnalysis;

import java.util.List;

public interface FaceAnalysisRepository extends JpaRepository<FaceAnalysis, Long> {

  List<FaceAnalysis> findBySessionId(Long sessionId);
}
