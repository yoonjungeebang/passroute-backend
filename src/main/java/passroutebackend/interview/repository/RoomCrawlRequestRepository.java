package passroutebackend.interview.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.interview.entity.RoomCrawlRequest;

public interface RoomCrawlRequestRepository extends JpaRepository<RoomCrawlRequest, Long> {

    Optional<RoomCrawlRequest> findByRoomId(Long roomId);
}
