package passroutebackend.interview.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuestionDto {

  private Long questionId;
  private String questionText;
  private int questionOrder;
  private String audioUrl;
}
