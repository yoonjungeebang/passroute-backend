package passroutebackend.interview.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import passroutebackend.global.ApiResponse;
import passroutebackend.interview.dto.response.CsTopicAnalysisResponse;
import passroutebackend.interview.service.CsTopicAnalysisService;

@Tag(name = "CsTopicAnalysis", description = "CS 토픽 분석 API")
@RestController
@RequestMapping("/interview/cs-topics")
@RequiredArgsConstructor
public class CsTopicAnalysisController {

    private final CsTopicAnalysisService csTopicAnalysisService;

    @Operation(summary = "CS 토픽 분석 조회", description = "사용자의 1:1 기술면접 이력을 CS 토픽별로 집계하여 강약점을 분석합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/analysis")
    public ResponseEntity<ApiResponse<CsTopicAnalysisResponse>> getAnalysis(
            @AuthenticationPrincipal Long userId) {
        CsTopicAnalysisResponse response = csTopicAnalysisService.getAnalysis(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
