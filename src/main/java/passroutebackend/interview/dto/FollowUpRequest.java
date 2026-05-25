package passroutebackend.interview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FollowUpRequest {

  @JsonProperty("interview_type")
  private final String interviewType;

  @JsonProperty("difficulty")
  private final String difficulty;

  @JsonProperty("conversation")
  private final List<QATurn> conversation;

  @JsonProperty("user_id")
  private final String userId;
}
