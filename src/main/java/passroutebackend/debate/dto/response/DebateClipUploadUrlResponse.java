package passroutebackend.debate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DebateClipUploadUrlResponse {

  private final String uploadUrl;
  private final String fileUrl;
}
