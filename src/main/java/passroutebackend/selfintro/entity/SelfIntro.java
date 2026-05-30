package passroutebackend.selfintro.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import passroutebackend.selfintro.dto.SelfIntroRequestDto;
import passroutebackend.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "self_introductions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SelfIntro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "job_position", nullable = false, length = 100)
    private String jobPosition;

    @Enumerated(EnumType.STRING)
    @Column(name = "career_level", nullable = false, length = 20)
    private CareerLevel careerLevel;

    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    @Column(name = "job_posting_url", length = 500)
    private String jobPostingUrl;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(name = "interview_date")
    private LocalDate interviewDate;

    @Column(name = "interview_time", length = 10)
    private String interviewTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_stage", length = 20)
    private InterviewStage interviewStage;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "version", nullable = false)
    private int version = 1;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "selfIntro", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNum ASC")
    private List<SelfIntroItem> items = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── 생성 ─────────────────────────────────────────────────

    public static SelfIntro create(User user, SelfIntroRequestDto dto) {
        SelfIntro selfIntro = new SelfIntro();
        selfIntro.user = user;
        selfIntro.apply(dto);
        return selfIntro;
    }

    // ── 수정 ─────────────────────────────────────────────────

    public void update(SelfIntroRequestDto dto) {
        apply(dto);
        this.version++;
    }

    public void updateItems(List<SelfIntroItem> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
    }

    // ── 소프트 딜리트 ─────────────────────────────────────────

    public void softDelete() {
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
    }

    // ── private ──────────────────────────────────────────────

    private void apply(SelfIntroRequestDto dto) {
        this.companyName = dto.getCompanyName();
        this.jobPosition = dto.getJobPosition();
        this.careerLevel = dto.getCareerLevel();
        this.jobDescription = dto.getJobDescription();
        this.jobPostingUrl = dto.getJobPostingUrl();
        this.memo = dto.getMemo();
        this.interviewDate = dto.getInterviewDate();
        this.interviewTime = dto.getInterviewTime();
        this.interviewStage = dto.getInterviewStage();
    }

    // ── Enum ─────────────────────────────────────────────────

    public enum CareerLevel {
        INTERN("인턴"),
        JUNIOR("신입"),
        SENIOR("경력");

        private final String label;

        CareerLevel(String label) { this.label = label; }

        @JsonCreator
        public static CareerLevel from(String value) {
            for (CareerLevel cl : values()) {
                if (cl.name().equalsIgnoreCase(value) || cl.label.equals(value)) return cl;
            }
            throw new IllegalArgumentException("Unknown CareerLevel: " + value);
        }
    }

    public enum InterviewStage {
        PERSONALITY("인성면접"),
        JOB("직무면접"),
        PRACTICAL("실무면접"),
        EXECUTIVE("임원면접"),
        TECHNICAL("기술면접");

        private final String label;

        InterviewStage(String label) { this.label = label; }

        @JsonCreator
        public static InterviewStage from(String value) {
            for (InterviewStage is : values()) {
                if (is.name().equalsIgnoreCase(value) || is.label.equals(value)) return is;
            }
            throw new IllegalArgumentException("Unknown InterviewStage: " + value);
        }
    }
}