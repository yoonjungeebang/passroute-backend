package passroutebackend.selfintro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import passroutebackend.selfintro.dto.SelfIntroRequestDto;
import passroutebackend.selfintro.dto.SelfIntroResponseDto;
import passroutebackend.selfintro.dto.response.SelfIntroReportResponse;
import passroutebackend.selfintro.service.SelfIntroReportService;
import passroutebackend.selfintro.service.SelfIntroService;

import java.util.List;

@Tag(name = "SelfIntro", description = "자소서 API")
@RestController
@RequestMapping("/self-intro")
@RequiredArgsConstructor
public class SelfIntroController {

    private final SelfIntroService selfIntroService;
    private final SelfIntroReportService selfIntroReportService;

    @GetMapping
    public ResponseEntity<List<SelfIntroResponseDto>> getList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "all") String filter) {
        return ResponseEntity.ok(selfIntroService.getList(userId, filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SelfIntroResponseDto> getOne(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(selfIntroService.getOne(userId, id));
    }

    @PostMapping
    public ResponseEntity<SelfIntroResponseDto> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid SelfIntroRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(selfIntroService.create(userId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SelfIntroResponseDto> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody @Valid SelfIntroRequestDto dto) {
        return ResponseEntity.ok(selfIntroService.update(userId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        selfIntroService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "자소서 리포트 조회", description = "자소서에 연결된 면접 세션들의 집계 리포트를 반환합니다. 응시 이력 0건이어도 200 + 빈 응답.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리포트 반환 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "자소서를 찾을 수 없음")
    })
    @GetMapping("/{id}/report")
    public ResponseEntity<SelfIntroReportResponse> getReport(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(selfIntroReportService.getReport(id, userId));
    }
}