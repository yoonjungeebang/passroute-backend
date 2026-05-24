package passroutebackend.document.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.document.dto.request.DocumentUploadCompleteRequest;
import passroutebackend.document.dto.response.DocumentListResponse;
import passroutebackend.document.dto.response.PresignedUrlResponse;
import passroutebackend.document.entity.Document;
import passroutebackend.document.entity.Document.DocumentType;
import passroutebackend.document.entity.Document.EmbedStatus;
import passroutebackend.document.repository.DocumentRepository;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.user.entity.User;
import passroutebackend.user.repository.UserRepository;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 1. Presigned URL 발급
    public PresignedUrlResponse generatePresignedUrl(Long userId, String filename, DocumentType type) {
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        String s3Key = "users/" + userId + "/" + type.name().toLowerCase() + "/" + UUID.randomUUID() + "." + ext;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return PresignedUrlResponse.builder()
                .presignedUrl(presignedRequest.url().toString())
                .s3Key(s3Key)
                .build();
    }

    // 2. 업로드 완료 후 DB 저장
    @Transactional
    public void uploadComplete(Long userId, DocumentUploadCompleteRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.of(ErrorCode.USER_NOT_FOUND));

        String ext = request.getOriginalFilename()
                .substring(request.getOriginalFilename().lastIndexOf(".") + 1).toLowerCase();

        Document document = Document.builder()
                .user(user)
                .type(request.getType())
                .s3Url("https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + request.getS3Key())
                .originalFilename(request.getOriginalFilename())
                .version(1)
                .isActive(true)
                .isRepresentative(false)
                .embedStatus(EmbedStatus.PENDING)
                .fileSize(request.getFileSize())
                .fileExt(ext)
                .uploadedAt(LocalDateTime.now())
                .build();

        documentRepository.save(document);
    }

    // 3. 파일 목록 조회
    @Transactional(readOnly = true)
    public DocumentListResponse getDocuments(Long userId, DocumentType type) {
        List<Document> documents = documentRepository
                .findByUserIdAndTypeAndDeletedAtIsNull(userId, type);
        return DocumentListResponse.of(documents);
    }

    // 4. 대표 파일 설정
    @Transactional
    public void setRepresentative(Long userId, Long documentId) {
        // document 먼저 조회
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> CustomException.of(ErrorCode.DOCUMENT_NOT_FOUND));

        // 기존 대표 파일 해제
        documentRepository
                .findByUserIdAndTypeAndIsRepresentativeTrueAndDeletedAtIsNull(userId, document.getType())
                .ifPresent(doc -> doc.setRepresentative(false));

        // 새 대표 파일 설정
        document.setRepresentative(true);
    }

    // 5. 파일 삭제
    @Transactional
    public void deleteDocument(Long userId, Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> CustomException.of(ErrorCode.DOCUMENT_NOT_FOUND));

        // S3에서도 삭제
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(extractS3Key(document.getS3Url()))
                .build());

        // DB soft delete
        document.delete();
    }

    private String extractS3Key(String s3Url) {
        return s3Url.substring(s3Url.indexOf(".amazonaws.com/") + ".amazonaws.com/".length());
    }
}