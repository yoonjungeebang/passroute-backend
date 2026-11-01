package passroutebackend.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import passroutebackend.document.dto.request.DocumentUploadCompleteRequest;
import passroutebackend.document.dto.response.DocumentListResponse;
import passroutebackend.document.dto.response.PresignedUrlResponse;
import passroutebackend.document.entity.Document.DocumentType;
import passroutebackend.document.service.DocumentService;
import passroutebackend.global.ApiResponse;
import passroutebackend.global.jwt.JwtTokenProvider;

@Tag(name = "Document", description = "문서(이력서/포트폴리오) 관리 API")
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Presigned URL 발급", description = "S3 직접 업로드를 위한 presigned URL과 S3 key를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Presigned URL 발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
            @AuthenticationPrincipal Long userId,
            @RequestParam String filename,
            @RequestParam DocumentType type) {

        PresignedUrlResponse response = documentService.generatePresignedUrl(userId, filename, type);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "업로드 완료 처리", description = "S3 업로드 완료 후 파일 정보를 DB에 저장합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업로드 완료 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/upload-complete")
    public ResponseEntity<ApiResponse<Void>> uploadComplete(
            @AuthenticationPrincipal Long userId,
            @RequestBody DocumentUploadCompleteRequest request) {

        documentService.uploadComplete(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "파일 목록 조회", description = "문서 타입별 파일 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<DocumentListResponse>> getDocuments(
            @AuthenticationPrincipal Long userId,
            @RequestParam DocumentType type) {

        DocumentListResponse response = documentService.getDocuments(userId, type);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "대표 파일 설정", description = "해당 문서를 대표 파일로 설정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "대표 파일 설정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문서를 찾을 수 없음")
    })
    @PatchMapping("/{documentId}/representative")
    public ResponseEntity<ApiResponse<Void>> setRepresentative(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long documentId) {

        documentService.setRepresentative(userId, documentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "파일 삭제", description = "문서를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문서를 찾을 수 없음")
    })
    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long documentId) {

        documentService.deleteDocument(userId, documentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
