package passroutebackend.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.document.entity.Document;
import passroutebackend.document.entity.DocumentAnalysis;

import java.util.Optional;

public interface DocumentAnalysisRepository extends JpaRepository<DocumentAnalysis, Long> {

    Optional<DocumentAnalysis> findByDocument(Document document);
}
