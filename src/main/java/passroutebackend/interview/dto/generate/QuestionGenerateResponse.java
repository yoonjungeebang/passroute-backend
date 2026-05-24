package passroutebackend.interview.dto.generate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class QuestionGenerateResponse {

  @JsonProperty("questions")
  private List<String> questions;
}
