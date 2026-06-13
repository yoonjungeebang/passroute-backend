package passroutebackend.debate.loader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import passroutebackend.debate.entity.AiPersona;
import passroutebackend.debate.entity.DebateStyle;
import passroutebackend.debate.repository.AiPersonaRepository;
import passroutebackend.interview.entity.Difficulty;

@ExtendWith(MockitoExtension.class)
class PersonaSeedLoaderTest {

  @Mock private AiPersonaRepository repository;
  @Mock private ApplicationArguments applicationArguments;

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  void loadsExistingPersonasOnceAndUpdatesVideoUrls() throws Exception {
    AiPersona existing = AiPersona.builder()
        .personaKey("persona_01_stable")
        .name("김지원")
        .debateStyle(DebateStyle.COOPERATIVE)
        .difficulty(Difficulty.NORMAL)
        .build();
    when(repository.findAll()).thenReturn(List.of(existing));
    when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

    new PersonaSeedLoader(repository, new ObjectMapper()).run(applicationArguments);

    verify(repository).findAll();
    ArgumentCaptor<List<AiPersona>> captor = ArgumentCaptor.forClass(List.class);
    verify(repository).saveAll(captor.capture());
    assertThat(captor.getValue()).hasSize(5);
    assertThat(existing.getSpeakingVideoUrl()).contains("speaking.mp4");
    assertThat(existing.getSilenceVideoUrl()).contains("idle.mp4");
  }
}
