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

  // AI 응답엔 없는 필드. 백엔드가 꼬리질문 여부를 주입(FE의 Q1-1 라벨링용).
  @JsonProperty("follow_up")
  private boolean followUp;

  public void setFollowUp(boolean followUp) {
    this.followUp = followUp;
  }
}
