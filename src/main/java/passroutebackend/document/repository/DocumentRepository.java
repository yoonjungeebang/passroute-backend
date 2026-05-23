package passroutebackend.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.document.entity.Document;
import passroutebackend.document.entity.Document.DocumentType;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    // 유저의 타입별 파일 목록 조회 (삭제 안 된 것만)
    List<Document> findByUserIdAndTypeAndDeletedAtIsNull(Long userId, DocumentType type);

    // 유저의 타입별 대표 파일 조회
    Optional<Document> findByUserIdAndTypeAndIsRepresentativeTrueAndDeletedAtIsNull(Long userId, DocumentType type);
}