package passroutebackend.interview.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class InterviewStartRequest {

  @NotNull
  private Long roomId;
}
