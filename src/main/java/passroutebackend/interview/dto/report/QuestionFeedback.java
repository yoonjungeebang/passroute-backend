package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuestionFeedback {

  @JsonProperty("question_index")
  private int questionIndex;

  private String question;

  @JsonProperty("question_type")
  private String questionType;

  private Double percentage;
  private String feedback;

  @JsonProperty("star_comment")
  private String starComment;

  @JsonProperty("voice_comment")
  private String voiceComment;
}
