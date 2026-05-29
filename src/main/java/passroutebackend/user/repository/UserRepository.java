package passroutebackend.user.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.user.entity.AuthProvider;
import passroutebackend.user.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    @EntityGraph(attributePaths = {"preferredCompanies", "preferredJobTypes"})
    Optional<User> findWithCollectionsById(Long id);
}
