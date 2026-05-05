package passroutebackend.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "phone_verifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder
public class PhoneVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private boolean verified;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public void verify() {
        this.verified = true;
        this.verifiedAt = LocalDateTime.now();
    }
}
