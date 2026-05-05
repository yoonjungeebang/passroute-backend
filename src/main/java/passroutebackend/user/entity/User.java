package passroutebackend.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_phone", columnNames = "phone"),
                @UniqueConstraint(name = "uk_users_provider_provider_id", columnNames = {"provider", "provider_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(length = 20)
    private String phone;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    @Builder.Default
    private boolean phoneVerified = false;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_preferred_job_types", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 50, nullable = false)
    @Builder.Default
    private Set<JobType> preferredJobTypes = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_preferred_companies", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "company_name", length = 100, nullable = false)
    @Builder.Default
    private Set<String> preferredCompanies = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
