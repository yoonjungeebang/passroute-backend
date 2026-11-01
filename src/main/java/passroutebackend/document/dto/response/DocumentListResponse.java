package passroutebackend.document.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.document.entity.Document;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "문서 목록 응답")
@Getter
@Builder
public class DocumentListResponse {

    @Schema(description = "문서 목록")
    private List<DocumentInfo> documents;

    public static DocumentListResponse of(List<Document> documents) {
        return DocumentListResponse.builder()
                .documents(documents.stream()
                        .map(DocumentInfo::from)
                        .toList())
                .build();
    }

    @Schema(description = "문서 상세 정보")
    @Getter
    @Builder
    public static class DocumentInfo {
        @Schema(description = "문서 ID")
        private Long id;
        @Schema(description = "원본 파일명")
        private String originalFilename;
        @Schema(description = "S3 URL")
        private String s3Url;
        @Schema(description = "대표 문서 여부")
        private Boolean isRepresentative;
        @Schema(description = "임베딩 상태")
        private String embedStatus;
        @Schema(description = "파일 확장자")
        private String fileExt;
        @Schema(description = "파일 크기 (bytes)")
        private Long fileSize;
        @Schema(description = "업로드 일시")
        private LocalDateTime uploadedAt;

        public static DocumentInfo from(Document document) {
            return DocumentInfo.builder()
                    .id(document.getId())
                    .originalFilename(document.getOriginalFilename())
                    .s3Url(document.getS3Url())
                    .isRepresentative(document.getIsRepresentative())
                    .embedStatus(document.getEmbedStatus().name())
                    .fileExt(document.getFileExt())
                    .fileSize(document.getFileSize())
                    .uploadedAt(document.getUploadedAt())
                    .build();
        }
    }
}