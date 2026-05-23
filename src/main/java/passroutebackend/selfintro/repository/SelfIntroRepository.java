package passroutebackend.selfintro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.selfintro.entity.SelfIntro;
import passroutebackend.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SelfIntroRepository extends JpaRepository<SelfIntro, Long> {

    // 전체 목록 (삭제 안 된 것만)
    List<SelfIntro> findByUserAndIsActiveTrueOrderByUpdatedAtDesc(User user);

    // 기간 필터 목록 (삭제 안 된 것만)
    List<SelfIntro> findByUserAndIsActiveTrueAndUpdatedAtAfterOrderByUpdatedAtDesc(
            User user, LocalDateTime from);

    // 단건 조회 (삭제 안 된 것만)
    Optional<SelfIntro> findByIdAndUserAndIsActiveTrue(Long id, User user);
}