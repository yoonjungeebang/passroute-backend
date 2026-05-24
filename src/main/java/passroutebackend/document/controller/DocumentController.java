package passroutebackend.document.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import passroutebackend.document.dto.request.DocumentUploadCompleteRequest;
import passroutebackend.document.dto.response.DocumentListResponse;
import passroutebackend.document.dto.response.PresignedUrlResponse;
import passroutebackend.document.entity.Document.DocumentType;
import passroutebackend.document.service.DocumentService;
import passroutebackend.global.ApiResponse;
import passroutebackend.global.jwt.JwtTokenProvider;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;
    private final JwtTokenProvider jwtTokenProvider;

    // 1. Presigned URL 발급
    @GetMapping("/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
            @AuthenticationPrincipal Long userId,
            @RequestParam String filename,
            @RequestParam DocumentType type) {

        PresignedUrlResponse response = documentService.generatePresignedUrl(userId, filename, type);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 2. 업로드 완료 후 DB 저장
    @PostMapping("/upload-complete")
    public ResponseEntity<ApiResponse<Void>> uploadComplete(
            @AuthenticationPrincipal Long userId,
            @RequestBody DocumentUploadCompleteRequest request) {

        documentService.uploadComplete(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 3. 파일 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<DocumentListResponse>> getDocuments(
            @AuthenticationPrincipal Long userId,
            @RequestParam DocumentType type) {

        DocumentListResponse response = documentService.getDocuments(userId, type);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 4. 대표 파일 설정
    @PatchMapping("/{documentId}/representative")
    public ResponseEntity<ApiResponse<Void>> setRepresentative(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long documentId) {

        documentService.setRepresentative(userId, documentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 5. 파일 삭제
    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long documentId) {

        documentService.deleteDocument(userId, documentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
