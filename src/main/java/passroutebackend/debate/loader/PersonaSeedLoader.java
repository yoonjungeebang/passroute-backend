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
import passroutebackend.debate.entity.AiPersona;
import passroutebackend.debate.entity.DebateStyle;
import passroutebackend.debate.repository.AiPersonaRepository;
import passroutebackend.interview.entity.Difficulty;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonaSeedLoader implements ApplicationRunner {

  private static final String SEED_PATH = "persona_seeds.yaml";

  private final AiPersonaRepository repository;
  private final ObjectMapper objectMapper;

  @Override
  @SuppressWarnings("unchecked")
  public void run(ApplicationArguments args) throws Exception {
    try (InputStream is = new ClassPathResource(SEED_PATH).getInputStream()) {
      Map<String, Object> root = new Yaml().load(is);
      if (root == null) {
        log.warn("persona_seeds.yaml이 비어 있습니다.");
        return;
      }
      List<Map<String, Object>> personas = (List<Map<String, Object>>) root.get("personas");
      if (personas == null) {
        log.warn("persona_seeds.yaml에 personas 키가 없음");
        return;
      }

      List<AiPersona> toSave = new ArrayList<>();
      int inserted = 0;
      int updated = 0;
      for (Map<String, Object> p : personas) {
        String personaKey = (String) p.get("personaId");
        String speakingVideoUrl = (String) p.get("speakingVideoUrl");
        String silenceVideoUrl = (String) p.get("silenceVideoUrl");

        AiPersona existing = repository.findByPersonaKey(personaKey).orElse(null);
        if (existing != null) {
          existing.updateVideoUrls(speakingVideoUrl, silenceVideoUrl);
          toSave.add(existing);
          updated++;
          continue;
        }
        toSave.add(AiPersona.builder()
            .personaKey(personaKey)
            .name((String) p.get("name"))
            .background((String) p.get("background"))
            .debateStyle(DebateStyle.valueOf((String) p.get("debateStyle")))
            .difficulty(Difficulty.valueOf((String) p.get("difficulty")))
            .strengths(toJson(p.get("strengths")))
            .weaknesses(toJson(p.get("weaknesses")))
            .systemPromptTemplate((String) p.get("systemPromptTemplate"))
            .speakingVideoUrl(speakingVideoUrl)
            .silenceVideoUrl(silenceVideoUrl)
            .build());
        inserted++;
      }
      repository.saveAll(toSave);
      log.info("페르소나 시드 적재 완료: inserted={}, updated={}", inserted, updated);
    } catch (Exception e) {
      log.error("페르소나 시드 적재 실패", e);
    }
  }

  private String toJson(Object obj) throws JsonProcessingException {
    return obj == null ? null : objectMapper.writeValueAsString(obj);
  }
}
