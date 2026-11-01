package passroutebackend.interview.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import passroutebackend.global.ApiResponse;
import passroutebackend.interview.dto.request.InterviewRoomRequest;
import passroutebackend.interview.dto.request.InterviewStartRequest;
import passroutebackend.interview.dto.response.InterviewRoomResponse;
import passroutebackend.interview.dto.response.InterviewStartResponse;
import passroutebackend.interview.service.CrawlStatusService;
import passroutebackend.interview.service.InterviewRoomService;
import passroutebackend.interview.service.InterviewStartService;

@Tag(name = "Interview", description = "면접 API")
@RestController
@RequestMapping("/interview")
@RequiredArgsConstructor
public class InterviewRoomController {

    private final InterviewRoomService interviewRoomService;
    private final InterviewStartService interviewStartService;
    private final CrawlStatusService crawlStatusService;

    @Operation(summary = "면접 설정", description = "면접 옵션을 설정하고 면접 방 ID를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "면접 방 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 입력"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/setup")
    public ResponseEntity<ApiResponse<InterviewRoomResponse>> setup(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody InterviewRoomRequest request) {
        InterviewRoomResponse response = interviewRoomService.setup(userId, request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @Operation(summary = "면접 시작", description = "면접 질문 준비를 요청하고 세션 ID와 상태를 즉시 반환합니다. "
            + "PREPARING 상태인 경우 SSE(/sessions/{id}/preparation-events)로 완료 알림을 수신하세요.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "면접 시작 요청 수락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "면접 방을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 준비 중이거나 진행 중인 세션 존재"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "동시 준비 수 초과")
    })
    @PostMapping("/start")
    public ApiResponse<InterviewStartResponse> start(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody InterviewStartRequest request) {
        return ApiResponse.success(interviewStartService.start(userId, request));
    }

    @Operation(summary = "크롤링 상태 구독", description = "면접방의 크롤링 수집 상태를 SSE로 실시간 수신합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "SSE 스트림 연결 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "면접 방을 찾을 수 없음")
    })
    @GetMapping(value = "/rooms/{roomId}/crawl-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter crawlEvents(@PathVariable Long roomId) {
        return crawlStatusService.subscribe(roomId);
    }
}
