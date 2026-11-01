package passroutebackend.interview.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "음성 분석 데이터")
@Getter
@NoArgsConstructor
public class VoiceData {

  @Schema(description = "불필요 단어 수", example = "3")
  @JsonProperty("filler_word_count")
  private Integer fillerWordCount;

  @Schema(description = "분당 단어 수", example = "120.5")
  @JsonProperty("wpm")
  private Double wpm;
}
