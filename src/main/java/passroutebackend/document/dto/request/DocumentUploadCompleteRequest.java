package passroutebackend.document.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import passroutebackend.document.entity.Document.DocumentType;

@Schema(description = "문서 업로드 완료 요청")
@Getter
public class DocumentUploadCompleteRequest {

    @Schema(description = "문서 타입", example = "RESUME")
    private DocumentType type;

    @Schema(description = "S3 저장 경로")
    private String s3Key;

    @Schema(description = "원본 파일명", example = "이력서_홍길동.pdf")
    private String originalFilename;

    @Schema(description = "파일 크기 (bytes)", example = "1048576")
    private Long fileSize;
}