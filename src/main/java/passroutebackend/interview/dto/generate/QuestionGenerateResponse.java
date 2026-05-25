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

  public List<String> getQuestionTexts() {
    return questions.stream()
        .map(QuestionItem::getQuestion)
        .toList();
  }

  @Getter
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class QuestionItem {
    @JsonProperty("question")
    private String question;
  }
}
