package passroutebackend.debate.loader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import passroutebackend.debate.entity.DebateTopic;
import passroutebackend.debate.entity.TopicCategory;
import passroutebackend.debate.repository.DebateTopicRepository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopicSeedLoader implements ApplicationRunner {

  private static final String SEED_PATH = "debate_topics.yaml";

  private final DebateTopicRepository repository;
  private final ObjectMapper objectMapper;

  @Override
  @SuppressWarnings("unchecked")
  public void run(ApplicationArguments args) throws Exception {
    try (InputStream is = new ClassPathResource(SEED_PATH).getInputStream()) {
      Map<String, Object> root = new Yaml().load(is);
      if (root == null) {
        log.warn("debate_topics.yaml이 비어 있습니다.");
        return;
      }
      List<Map<String, Object>> topics = (List<Map<String, Object>>) root.get("topics");
      if (topics == null) {
        log.warn("debate_topics.yaml에 topics 키가 없음");
        return;
      }

      List<DebateTopic> toInsert = new ArrayList<>();
      int skipped = 0;
      for (Map<String, Object> t : topics) {
        String topicKey = (String) t.get("id");
        if (repository.findByTopicKey(topicKey).isPresent()) {
          skipped++;
          continue;
        }
        toInsert.add(DebateTopic.builder()
            .topicKey(topicKey)
            .title((String) t.get("title"))
            .description((String) t.get("description"))
            .category(TopicCategory.valueOf((String) t.get("category")))
            .proKeyPoints(toJson(t.get("proKeyPoints")))
            .conKeyPoints(toJson(t.get("conKeyPoints")))
            .build());
      }
      repository.saveAll(toInsert);
      log.info("토론 주제 시드 적재 완료: inserted={}, skipped={}", toInsert.size(), skipped);
    } catch (Exception e) {
      log.error("토론 주제 시드 적재 실패", e);
    }
  }

  private String toJson(Object obj) throws JsonProcessingException {
    return obj == null ? null : objectMapper.writeValueAsString(obj);
  }
}
