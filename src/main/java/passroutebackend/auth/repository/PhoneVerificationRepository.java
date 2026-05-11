package passroutebackend.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.auth.entity.PhoneVerification;

import java.util.Optional;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {
    Optional<PhoneVerification> findTopByPhoneOrderByCreatedAtDesc(String phone);
    void deleteByPhone(String phone);
}
