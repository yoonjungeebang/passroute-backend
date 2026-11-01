package passroutebackend.interview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "클립 업로드 URL 응답")
@Getter
@AllArgsConstructor
public class ClipUploadUrlResponse {

  @Schema(description = "S3 업로드용 presigned URL")
  private final String uploadUrl;
  @Schema(description = "업로드 완료 후 접근할 파일 URL")
  private final String fileUrl;
}
