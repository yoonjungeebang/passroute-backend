package passroutebackend.interview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VoiceData {

  @JsonProperty("filler_word_count")
  private Integer fillerWordCount;

  @JsonProperty("wpm")
  private Double wpm;
}
