package passroutebackend.interview.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.interview.entity.SessionStatus;

@Getter
@AllArgsConstructor
public class InterviewStartResponse {

  private Long sessionId;
  private SessionStatus status;

  public static InterviewStartResponse preparing(Long sessionId) {
    return new InterviewStartResponse(sessionId, SessionStatus.PREPARING);
  }

  public static InterviewStartResponse existing(Long sessionId, SessionStatus status) {
    return new InterviewStartResponse(sessionId, status);
  }
}
