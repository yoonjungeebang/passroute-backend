package passroutebackend.interview.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import passroutebackend.global.ApiResponse;
import passroutebackend.interview.dto.request.InterviewRoomRequestDto;
import passroutebackend.interview.dto.request.InterviewStartRequest;
import passroutebackend.interview.dto.response.InterviewRoomResponseDto;
import passroutebackend.interview.dto.response.InterviewStartResponse;
import passroutebackend.interview.service.InterviewRoomService;
import passroutebackend.interview.service.InterviewStartService;

@Tag(name = "Interview", description = "면접 API")
@RestController
@RequestMapping("/interview")
@RequiredArgsConstructor
public class InterviewRoomController {

    private final InterviewRoomService interviewRoomService;
    private final InterviewStartService interviewStartService;

    @Operation(summary = "면접 설정", description = "면접 옵션을 설정하고 면접 방 ID를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "면접 방 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 입력"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/setup")
    public ResponseEntity<ApiResponse<InterviewRoomResponseDto>> setup(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody InterviewRoomRequestDto request) {
        InterviewRoomResponseDto response = interviewRoomService.setup(userId, request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @Operation(summary = "면접 시작", description = "면접 세션을 생성하고 AI가 생성한 질문 목록을 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "면접 세션 생성 및 질문 반환 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "면접 방을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "AI 서버 오류")
    })
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<InterviewStartResponse>> start(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody InterviewStartRequest request) {
        InterviewStartResponse response = interviewStartService.start(userId, request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }
}
