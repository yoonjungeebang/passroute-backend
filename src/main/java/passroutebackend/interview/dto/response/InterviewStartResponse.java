package passroutebackend.interview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.interview.entity.SessionStatus;

@Schema(description = "면접 시작 응답")
@Getter
@AllArgsConstructor
public class InterviewStartResponse {

  @Schema(description = "세션 ID")
  private Long sessionId;
  @Schema(description = "세션 상태")
  private SessionStatus status;

  public static InterviewStartResponse preparing(Long sessionId) {
    return new InterviewStartResponse(sessionId, SessionStatus.PREPARING);
  }

  public static InterviewStartResponse existing(Long sessionId, SessionStatus status) {
    return new InterviewStartResponse(sessionId, status);
  }
}
