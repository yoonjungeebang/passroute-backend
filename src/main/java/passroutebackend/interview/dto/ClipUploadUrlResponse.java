package passroutebackend.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClipUploadUrlResponse {

  private final String uploadUrl;
  private final String fileUrl;
}
