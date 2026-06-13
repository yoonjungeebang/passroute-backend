package passroutebackend.interview.dto.generate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionGenerateResponse {

  @JsonProperty("questions")
  private List<QuestionItem> questions;

  @Getter
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class QuestionItem {
    @JsonProperty("question")
    private String question;

    // ONE_ON_ONE만 값 존재, 토론/미지원/합성 실패 시 null
    @JsonProperty("audio_url")
    private String audioUrl;

    // 1:1 기술 면접만 값 존재, 인성/토론 면접은 null
    @JsonProperty("cs_topic")
    private String csTopic;
  }
}
