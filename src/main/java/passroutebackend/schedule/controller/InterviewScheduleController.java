package passroutebackend.schedule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import passroutebackend.global.ApiResponse;
import passroutebackend.interview.dto.response.InterviewRoomResponseDto;
import passroutebackend.schedule.dto.request.ScheduleCreateRequest;
import passroutebackend.schedule.dto.request.ScheduleStatusUpdateRequest;
import passroutebackend.schedule.dto.request.ScheduleUpdateRequest;
import passroutebackend.schedule.dto.response.ScheduleCalendarResponse;
import passroutebackend.schedule.dto.response.ScheduleResponse;
import passroutebackend.schedule.service.InterviewScheduleService;

import java.util.List;

@Tag(name = "Schedule", description = "면접 일정 관리 API")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class InterviewScheduleController {

    private final InterviewScheduleService interviewScheduleService;

    @Operation(summary = "면접 일정 생성", description = "새로운 면접 일정을 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "일정 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 입력"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleResponse>> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ScheduleCreateRequest request) {
        ScheduleResponse response = interviewScheduleService.create(userId, request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @Operation(summary = "면접 일정 목록 조회", description = "사용자의 면접 일정 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getList(
            @AuthenticationPrincipal Long userId) {
        List<ScheduleResponse> response = interviewScheduleService.getList(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "월별 면접 일정 캘린더 조회", description = "지정한 연·월의 면접 일정을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<ScheduleCalendarResponse>> getCalendar(
            @AuthenticationPrincipal Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        ScheduleCalendarResponse response = interviewScheduleService.getCalendar(userId, year, month);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "면접 일정 상세 조회", description = "면접 일정의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long scheduleId) {
        ScheduleResponse response = interviewScheduleService.getDetail(userId, scheduleId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "면접 일정 수정", description = "면접 일정 정보를 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleUpdateRequest request) {
        ScheduleResponse response = interviewScheduleService.update(userId, scheduleId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "면접 일정 삭제", description = "면접 일정을 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long scheduleId) {
        interviewScheduleService.delete(userId, scheduleId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "면접 일정 상태 변경", description = "면접 일정의 상태를 변경합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @PatchMapping("/{scheduleId}/status")
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleStatusUpdateRequest request) {
        ScheduleResponse response = interviewScheduleService.updateStatus(userId, scheduleId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "면접 일정에서 바로 연습하기", description = "면접 일정 정보를 기반으로 연습 면접 방을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "연습 방 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
    })
    @PostMapping("/{scheduleId}/practice")
    public ResponseEntity<ApiResponse<InterviewRoomResponseDto>> practice(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long scheduleId) {
        Long roomId = interviewScheduleService.practice(userId, scheduleId);
        return ResponseEntity.status(201).body(ApiResponse.created(new InterviewRoomResponseDto(roomId)));
    }
}
