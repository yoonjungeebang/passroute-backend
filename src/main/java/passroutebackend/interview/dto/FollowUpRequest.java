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
  private final Long userId;

  // 원질문과 같은 목소리(TTS) 유지를 위한 면접관 persona
  @JsonProperty("persona")
  private final String persona;
}
