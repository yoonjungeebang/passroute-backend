package passroutebackend.interview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FollowUpResponse {

  @JsonProperty("has_follow_up")
  private final boolean hasFollowUp;

  @JsonProperty("follow_up_question")
  private final String followUpQuestion;

  @JsonProperty("reason")
  private final String reason;

  public static FollowUpResponse noFollowUp() {
    return new FollowUpResponse(false, null, null);
  }
}
