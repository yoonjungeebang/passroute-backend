package passroutebackend.document.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PresignedUrlResponse {

    private String presignedUrl;  // 프론트가 S3에 업로드할 URL
    private String s3Key;         // 업로드 완료 후 백엔드에 알려줄 경로
}