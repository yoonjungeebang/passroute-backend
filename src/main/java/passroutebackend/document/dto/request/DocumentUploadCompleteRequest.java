package passroutebackend.document.dto.request;

import lombok.Getter;
import passroutebackend.document.entity.Document.DocumentType;

@Getter
public class DocumentUploadCompleteRequest {

    private DocumentType type;        // RESUME | PORTFOLIO
    private String s3Key;             // S3 저장 경로
    private String originalFilename;  // 원본 파일명
    private Long fileSize;            // 파일 크기 (bytes)
}