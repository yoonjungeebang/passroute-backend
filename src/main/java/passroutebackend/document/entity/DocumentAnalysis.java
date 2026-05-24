package passroutebackend.document.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_analyses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @Builder
    public DocumentAnalysis(Document document, String extractedText, LocalDateTime analyzedAt) {
        this.document = document;
        this.extractedText = extractedText;
        this.analyzedAt = analyzedAt;
    }
}
