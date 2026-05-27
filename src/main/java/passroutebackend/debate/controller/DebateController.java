package passroutebackend.debate.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import passroutebackend.debate.dto.request.DebateSessionCreateRequest;
import passroutebackend.debate.dto.request.DebateTurnSubmitRequest;
import passroutebackend.debate.dto.response.DebatePersonaResponse;
import passroutebackend.debate.dto.response.DebateSessionCreateResponse;
import passroutebackend.debate.dto.response.DebateStateResponse;
import passroutebackend.debate.dto.response.DebateTopicResponse;
import passroutebackend.debate.entity.TopicCategory;
import passroutebackend.debate.service.DebateReportService;
import passroutebackend.debate.service.DebateService;
import passroutebackend.global.ApiResponse;

import java.util.List;

@Tag(name = "Debate", description = "토론 면접 API")
@RestController
@RequestMapping("/debate")
@RequiredArgsConstructor
public class DebateController {

  private final DebateService debateService;
  private final DebateReportService reportService;

  @Operation(summary = "토론 주제 목록 조회")
  @GetMapping("/topics")
  public ResponseEntity<ApiResponse<List<DebateTopicResponse>>> listTopics(
      @RequestParam(required = false) TopicCategory category) {
    return ResponseEntity.ok(ApiResponse.success(debateService.listTopics(category)));
  }

  @Operation(summary = "AI 페르소나 목록 조회")
  @GetMapping("/personas")
  public ResponseEntity<ApiResponse<List<DebatePersonaResponse>>> listPersonas() {
    return ResponseEntity.ok(ApiResponse.success(debateService.listPersonas()));
  }

  @Operation(summary = "토론 세션 생성")
  @PostMapping("/sessions")
  public ResponseEntity<ApiResponse<DebateSessionCreateResponse>> createSession(
      @AuthenticationPrincipal Long userId,
      @Valid @RequestBody DebateSessionCreateRequest request) {
    DebateSessionCreateResponse response = debateService.createSession(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
  }

  @Operation(summary = "토론 시작 (면접관 오프닝 비동기 생성)")
  @PostMapping("/{sessionId}/start")
  public ResponseEntity<ApiResponse<Void>> startSession(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long sessionId) {
    debateService.startSession(userId, sessionId);
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(ApiResponse.accepted("토론이 시작되었습니다."));
  }

  @Operation(summary = "토론 상태 + 턴 목록 조회 (폴링)")
  @GetMapping("/{sessionId}/state")
  public ResponseEntity<ApiResponse<DebateStateResponse>> getState(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long sessionId) {
    return ResponseEntity.ok(ApiResponse.success(debateService.getState(userId, sessionId)));
  }

  @Operation(summary = "사용자 발화 제출")
  @PostMapping("/{sessionId}/turn")
  public ResponseEntity<ApiResponse<Void>> submitTurn(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long sessionId,
      @Valid @RequestBody DebateTurnSubmitRequest request) {
    debateService.submitUserTurn(userId, sessionId, request);
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(ApiResponse.accepted("발화가 제출되었습니다."));
  }

  @Operation(summary = "토론 종료 + 리포트 비동기 생성")
  @PostMapping("/{sessionId}/end")
  public ResponseEntity<ApiResponse<Void>> endSession(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long sessionId) {
    debateService.endSession(userId, sessionId);
    reportService.generateReportAsync(sessionId, userId);
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(ApiResponse.accepted("리포트를 생성 중입니다."));
  }

}
