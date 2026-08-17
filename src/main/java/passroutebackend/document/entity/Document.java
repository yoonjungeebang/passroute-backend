package passroutebackend.document.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import passroutebackend.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType type;

    @Column(name = "s3_url", nullable = false, length = 512)
    private String s3Url;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_representative", nullable = false)
    private Boolean isRepresentative;

    @Enumerated(EnumType.STRING)
    @Column(name = "embed_status", nullable = false)
    private EmbedStatus embedStatus;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_ext", length = 10)
    private String fileExt;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    // 대표 파일 설정
    public void setRepresentative(boolean isRepresentative) {
        this.isRepresentative = isRepresentative;
    }

    // soft delete
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }

    // 파일 이름 변경
    public void updateFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    // 텍스트 추출 상태 업데이트
    public void updateEmbedStatus(EmbedStatus embedStatus) {
        this.embedStatus = embedStatus;
    }

    public enum DocumentType {
        RESUME, PORTFOLIO
    }

    public enum EmbedStatus {
        PENDING, DONE, FAILED
    }
}
