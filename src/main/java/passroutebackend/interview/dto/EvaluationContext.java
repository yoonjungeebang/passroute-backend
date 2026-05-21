package passroutebackend.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationContext {

  private Long answerId;
  private String questionText;
  private String answerText;
  private String interviewType;
  private String difficulty;
  private String jobPosition;
  private String companyName;
}
