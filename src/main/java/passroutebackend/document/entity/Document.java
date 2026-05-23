package passroutebackend.document.entity;

import jakarta.persistence.*;
import lombok.*;
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

    public enum DocumentType {
        RESUME, PORTFOLIO
    }

    public enum EmbedStatus {
        PENDING, DONE, FAILED
    }
}
