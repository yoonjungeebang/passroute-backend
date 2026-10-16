package passroutebackend.interview.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_crawl_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomCrawlRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CrawlRequestStatus status;

    @Column(columnDefinition = "TEXT")
    private String resultPayload;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @Builder
    public RoomCrawlRequest(Long roomId, CrawlRequestStatus status) {
        this.roomId = roomId;
        this.status = status;
    }

    public void updateStatus(CrawlRequestStatus status) {
        this.status = status;
    }

    public void markCompleted(String resultPayload) {
        this.status = CrawlRequestStatus.COMPLETED;
        this.resultPayload = resultPayload;
        this.completedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = CrawlRequestStatus.FAILED;
        this.completedAt = LocalDateTime.now();
    }

    public void markNoData() {
        this.status = CrawlRequestStatus.NO_DATA;
        this.completedAt = LocalDateTime.now();
    }
}
