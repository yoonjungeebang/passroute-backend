package passroutebackend.interview.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import passroutebackend.global.ApiResponse;
import passroutebackend.interview.dto.AnswerSubmitRequest;
import passroutebackend.interview.dto.response.AnswerProgressResponse;
import passroutebackend.interview.dto.response.SessionQuestionListResponse;
import passroutebackend.interview.service.InterviewProgressService;

@Tag(name = "Interview", description = "면접 API")
@RestController
@RequestMapping("/interview/sessions")
@RequiredArgsConstructor
public class InterviewProgressController {

  private final InterviewProgressService progressService;

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
    return ApiResponse.success(progressService.getQuestions(sessionId, userId));
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
    return ApiResponse.success(progressService.submitAnswer(sessionId, userId, request));
  }

  @Operation(summary = "면접 종료", description = "면접 세션을 종료하고 세션·방 상태를 COMPLETED로 업데이트합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "면접 종료 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 종료된 세션"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
  })
  @PostMapping("/{sessionId}/end")
  public ApiResponse<Void> endSession(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long sessionId) {
    progressService.endSession(sessionId, userId);
    return ApiResponse.success();
  }
}
