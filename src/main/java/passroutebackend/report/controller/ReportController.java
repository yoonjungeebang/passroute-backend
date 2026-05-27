package passroutebackend.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import passroutebackend.debate.entity.DebateReport;
import passroutebackend.debate.dto.response.DebateReportApiResponse;
import passroutebackend.debate.service.DebateReportService;
import passroutebackend.global.ApiResponse;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.dto.report.InterviewReportResponse;
import passroutebackend.interview.entity.InterviewReport;
import passroutebackend.interview.entity.ReportStatus;
import passroutebackend.interview.service.ReportService;
import passroutebackend.selfintro.dto.response.SelfIntroReportResponse;
import passroutebackend.selfintro.service.SelfIntroReportService;

@Tag(name = "Report", description = "리포트 통합 API")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

  private final ReportService reportService;
  private final DebateReportService debateReportService;
  private final SelfIntroReportService selfIntroReportService;

  @Operation(summary = "면접 리포트 조회", description = "면접 세션의 단건 리포트. 생성 중 202, 완료 200, FAILED 500.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리포트 반환 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "리포트 생성 중"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "아직 종료되지 않은 세션"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "리포트 생성 실패")
  })
  @GetMapping("/interview/{sessionId}")
  public ResponseEntity<ApiResponse<InterviewReportResponse>> getInterviewReport(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long sessionId) {
    Optional<InterviewReport> reportOpt = reportService.findReport(sessionId, userId);
    if (reportOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.ACCEPTED)
          .body(ApiResponse.accepted("리포트 생성 중입니다."));
    }
    InterviewReport report = reportOpt.get();
    if (report.getReportStatus() == ReportStatus.FAILED) {
      throw CustomException.of(ErrorCode.REPORT_GENERATION_FAILED);
    }
    return ResponseEntity.ok(ApiResponse.success(reportService.toResponseDto(report)));
  }

  @Operation(summary = "토론 리포트 조회", description = "토론 세션의 단건 리포트. 생성 중 202, 완료 200, FAILED 500.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리포트 반환 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "리포트 생성 중"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "리포트 생성 실패")
  })
  @GetMapping("/debate/{sessionId}")
  public ResponseEntity<ApiResponse<DebateReportApiResponse>> getDebateReport(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long sessionId) {
    Optional<DebateReport> reportOpt = debateReportService.findReport(userId, sessionId);
    if (reportOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.ACCEPTED)
          .body(ApiResponse.accepted("리포트 생성 중입니다."));
    }
    DebateReport report = reportOpt.get();
    if (report.getReportStatus() == ReportStatus.FAILED) {
      throw CustomException.of(ErrorCode.REPORT_GENERATION_FAILED);
    }
    return ResponseEntity.ok(ApiResponse.success(debateReportService.toApiResponse(report)));
  }

  @Operation(summary = "자소서 리포트 조회", description = "자소서에 연결된 면접 세션들의 집계 리포트. 응시 이력 0건이어도 200 + 빈 응답.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리포트 반환 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "자소서를 찾을 수 없음")
  })
  @GetMapping("/self-intro/{selfIntroId}")
  public ResponseEntity<ApiResponse<SelfIntroReportResponse>> getSelfIntroReport(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long selfIntroId) {
    return ResponseEntity.ok(ApiResponse.success(selfIntroReportService.getReport(selfIntroId, userId)));
  }
}
