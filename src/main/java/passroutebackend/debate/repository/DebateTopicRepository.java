package passroutebackend.debate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import passroutebackend.debate.entity.DebateTopic;
import passroutebackend.debate.entity.TopicCategory;

import java.util.List;
import java.util.Optional;

public interface DebateTopicRepository extends JpaRepository<DebateTopic, Long> {

  // 시드 로더 멱등 적재용
  Optional<DebateTopic> findByTopicKey(String topicKey);

  // 카테고리별 조회 (GET /debate/topics?category=...)
  List<DebateTopic> findByCategory(TopicCategory category);
}
