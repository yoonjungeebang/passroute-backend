package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuestionEvaluationForReport {

  @JsonProperty("question_index")
  private int questionIndex;

  @JsonProperty("question_type")
  private String questionType;

  private String question;

  // STT 답변 원문 (AI가 답변 인용 기반 구체 피드백 생성에 사용). optional, null 허용.
  @JsonProperty("answer")
  private String answer;

  private Double percentage;
  private QuestionSummary summary;

  @JsonProperty("star_evaluation")
  private StarEvalForReport starEvaluation;

  @JsonProperty("voice_feedback")
  private String voiceFeedback;
}
