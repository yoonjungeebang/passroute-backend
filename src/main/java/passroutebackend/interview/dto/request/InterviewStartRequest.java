package passroutebackend.interview.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Schema(description = "면접 시작 요청")
@Getter
public class InterviewStartRequest {

  @Schema(description = "면접 방 ID")
  @NotNull
  private Long roomId;
}
