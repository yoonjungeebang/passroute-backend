package passroutebackend.interview.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import passroutebackend.global.ApiResponse;
import passroutebackend.interview.dto.AnswerSubmitRequest;
import passroutebackend.interview.dto.ClipUploadUrlResponse;
import passroutebackend.interview.dto.SaveWorstClipRequest;
import passroutebackend.interview.dto.response.AnswerProgressResponse;
import passroutebackend.interview.dto.response.SessionQuestionListResponse;
import passroutebackend.interview.service.InterviewSessionService;

@Tag(name = "Interview", description = "면접 API")
@RestController
@RequestMapping("/interview/sessions")
@RequiredArgsConstructor
public class InterviewSessionController {

  private final InterviewSessionService sessionService;

  @Operation(summary = "질문 목록 조회", description = "세션의 질문 목록을 questionOrder 오름차순으로 반환합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "질문 목록 반환 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
  })
  @GetMapping("/{sessionId}/questions")
  public ApiResponse<SessionQuestionListResponse> getQuestions(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long sessionId) {
    return ApiResponse.success(sessionService.getQuestions(sessionId, userId));
  }

  @Operation(summary = "답변 제출", description = "답변을 저장하고 꼬리질문 생성 여부 및 마지막 질문 여부를 반환합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "답변 저장 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 종료된 세션"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션 또는 질문을 찾을 수 없음")
  })
  @PostMapping("/{sessionId}/answers")
  public ApiResponse<AnswerProgressResponse> submitAnswer(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long sessionId,
      @Valid @RequestBody AnswerSubmitRequest request) {
    return ApiResponse.success(sessionService.submitAnswer(sessionId, userId, request));
  }

  @Operation(summary = "면접 종료", description = "면접 세션을 종료하고 세션·방 상태를 COMPLETED로 업데이트한 뒤 리포트 비동기 생성을 트리거합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "면접 종료 및 리포트 생성 요청 수락"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 종료된 세션"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
  })
  @PostMapping("/{sessionId}/end")
  public ResponseEntity<ApiResponse<Void>> endSession(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long sessionId) {
    sessionService.endSession(sessionId, userId);
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(ApiResponse.accepted("리포트를 생성 중입니다."));
  }

  @Operation(summary = "최저 점수 클립 URL 조회", description = "세션에서 clip_score가 가장 낮은 답변의 S3 영상 URL을 반환합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "URL 반환 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
  })
  @GetMapping("/{sessionId}/worst-clip")
  public ApiResponse<String> getWorstClip(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long sessionId) {
    return ApiResponse.success(sessionService.getWorstClipVideoUrl(sessionId, userId));
  }

  @Operation(summary = "클립 업로드용 Presigned URL 발급", description = "S3에 영상을 직접 PUT 업로드할 수 있는 presigned URL과 업로드 완료 후 접근할 fileUrl을 반환합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Presigned URL 발급 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
  })
  @GetMapping("/{sessionId}/clip-upload-url")
  public ApiResponse<ClipUploadUrlResponse> getClipUploadUrl(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long sessionId,
      @RequestParam Long questionId) {
    return ApiResponse.success(sessionService.getClipUploadUrl(sessionId, userId, questionId));
  }

  @Operation(summary = "최악 클립 저장", description = "업로드된 클립의 S3 URL과 점수를 해당 답변에 저장합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "저장 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션 또는 답변을 찾을 수 없음")
  })
  @PostMapping("/{sessionId}/worst-clip")
  public ApiResponse<Void> saveWorstClip(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long sessionId,
      @Valid @RequestBody SaveWorstClipRequest request) {
    sessionService.saveWorstClip(sessionId, userId, request);
    return ApiResponse.success(null);
  }
}
