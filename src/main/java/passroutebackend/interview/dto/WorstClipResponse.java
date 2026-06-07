package passroutebackend.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorstClipResponse {

  private final String videoUrl;
  private final String clipReason;
}
