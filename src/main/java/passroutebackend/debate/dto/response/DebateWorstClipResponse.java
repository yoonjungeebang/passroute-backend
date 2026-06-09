package passroutebackend.debate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DebateWorstClipResponse {

  private final String videoUrl;
  private final String clipReason;
}
