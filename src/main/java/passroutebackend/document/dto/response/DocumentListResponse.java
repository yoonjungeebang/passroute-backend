package passroutebackend.document.dto.response;

import lombok.Builder;
import lombok.Getter;
import passroutebackend.document.entity.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DocumentListResponse {

    private List<DocumentInfo> documents;

    public static DocumentListResponse of(List<Document> documents) {
        return DocumentListResponse.builder()
                .documents(documents.stream()
                        .map(DocumentInfo::from)
                        .toList())
                .build();
    }

    @Getter
    @Builder
    public static class DocumentInfo {
        private Long id;
        private String originalFilename;
        private String s3Url;
        private Boolean isRepresentative;
        private String embedStatus;
        private String fileExt;
        private Long fileSize;
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