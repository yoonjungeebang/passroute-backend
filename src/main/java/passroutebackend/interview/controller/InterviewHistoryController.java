package passroutebackend.interview.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import passroutebackend.global.ApiResponse;
import passroutebackend.interview.dto.response.HistoryDetailResponse;
import passroutebackend.interview.dto.response.HistoryListResponse;
import passroutebackend.interview.dto.response.InterviewRoomResponseDto;
import passroutebackend.interview.entity.InterviewFormat;
import passroutebackend.interview.entity.InterviewType;
import passroutebackend.interview.service.InterviewHistoryService;

@Tag(name = "History", description = "면접 이력 API")
@RestController
@RequestMapping("/api/histories")
@RequiredArgsConstructor
public class InterviewHistoryController {

    private final InterviewHistoryService interviewHistoryService;

    @Operation(summary = "면접 이력 목록 조회", description = "완료된 면접 이력을 목록으로 조회합니다. 카테고리별 필터링이 가능합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<HistoryListResponse>>> getList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) InterviewType type,
            @RequestParam(required = false) InterviewFormat format,
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<HistoryListResponse> response = interviewHistoryService.getList(userId, type, format, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "면접 이력 상세 조회", description = "면접 이력의 상세 정보(질문, 답변, 평가)를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "면접 방을 찾을 수 없음")
    })
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<HistoryDetailResponse>> getDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId) {
        HistoryDetailResponse response = interviewHistoryService.getDetail(userId, roomId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "면접 다시하기", description = "기존 면접 설정을 복제하여 새로운 면접 방을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "면접 방 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "면접 방을 찾을 수 없음")
    })
    @PostMapping("/{roomId}/retry")
    public ResponseEntity<ApiResponse<InterviewRoomResponseDto>> retry(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId) {
        InterviewRoomResponseDto response = interviewHistoryService.retry(userId, roomId);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @Operation(summary = "면접 이력 텍스트 검색", description = "기업명, 직무, 질문 내용으로 면접 이력을 검색합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<HistoryListResponse>>> search(
            @AuthenticationPrincipal Long userId,
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<HistoryListResponse> response = interviewHistoryService.search(userId, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
