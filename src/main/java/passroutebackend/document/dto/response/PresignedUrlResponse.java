package passroutebackend.document.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "Presigned URL 응답")
@Getter
@Builder
public class PresignedUrlResponse {

    @Schema(description = "S3 업로드용 presigned URL")
    private String presignedUrl;

    @Schema(description = "업로드 완료 후 백엔드에 전달할 S3 key")
    private String s3Key;
}