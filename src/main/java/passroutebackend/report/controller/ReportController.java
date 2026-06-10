package passroutebackend.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import passroutebackend.report.dto.response.ReportListResponse;
import passroutebackend.report.dto.response.UpcomingReportResponse;
import passroutebackend.report.service.ReportListService;
import passroutebackend.report.service.UpcomingReportService;
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
  private final ReportListService reportListService;
  private final UpcomingReportService upcomingReportService;

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
    ReportStatus status = report.getReportStatus();
    if (status == ReportStatus.FAILED) {
      // 답변이 0개면 영구 실패(재시도 무의미) → I013, 그 외엔 일시 실패(재시도 가능) → I010
      if (reportService.hasNoAnswers(sessionId)) {
        throw CustomException.of(ErrorCode.REPORT_NO_ANSWERS);
      }
      throw CustomException.of(ErrorCode.REPORT_GENERATION_FAILED);
    }
    if (status == ReportStatus.GENERATING) {
      // 임계값 초과(워커 사망 등)면 FAILED 전환 후 실패 응답 → 무한 폴링 차단
      if (reportService.failIfStale(report)) {
        throw CustomException.of(ErrorCode.REPORT_GENERATION_FAILED);
      }
      return ResponseEntity.status(HttpStatus.ACCEPTED)
          .body(ApiResponse.accepted("리포트 생성 중입니다."));
    }
    return ResponseEntity.ok(ApiResponse.success(reportService.toResponseDto(report)));
  }

  @Operation(summary = "면접 리포트 삭제", description = "면접 세션(=리포트) 1건 소프트 삭제. 멱등(이미 삭제 시에도 200).")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
  })
  @DeleteMapping("/interview/{sessionId}")
  public ResponseEntity<ApiResponse<Void>> deleteInterviewReport(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long sessionId) {
    reportService.deleteInterviewReport(sessionId, userId);
    return ResponseEntity.ok(ApiResponse.success());
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

  @Operation(summary = "토론 리포트 삭제", description = "토론 세션(=리포트) 1건 소프트 삭제. 멱등(이미 삭제 시에도 200).")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
  })
  @DeleteMapping("/debate/{sessionId}")
  public ResponseEntity<ApiResponse<Void>> deleteDebateReport(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long sessionId) {
    debateReportService.deleteDebateReport(userId, sessionId);
    return ResponseEntity.ok(ApiResponse.success());
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

  @Operation(summary = "리포트 통합 목록 조회", description = "면접·토론 리포트를 통합한 페이지네이션 목록. 자소서 리포트는 단건 API로만 제공.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리스트 반환 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 type/page/size")
  })
  @GetMapping
  public ResponseEntity<ApiResponse<ReportListResponse>> getReports(
      @AuthenticationPrincipal Long userId,
      @RequestParam(required = false) Long resumeId,
      @RequestParam(required = false, defaultValue = "all") String type,
      @RequestParam(required = false) String q,
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "20") int size) {
    return ResponseEntity.ok(ApiResponse.success(
        reportListService.getReports(userId, resumeId, type, q, page, size)));
  }

  @Operation(summary = "다가오는 면접 + 이전 회차 분석", description = "사용자의 미래 면접 일정과 같은 기업의 이전 회차 리포트 요약을 반환합니다. AI 종합 피드백은 MVP에서 항상 null.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "다가오는 면접 목록 반환 성공")
  })
  @GetMapping("/upcoming")
  public ResponseEntity<ApiResponse<UpcomingReportResponse>> getUpcoming(
      @AuthenticationPrincipal Long userId) {
    return ResponseEntity.ok(ApiResponse.success(upcomingReportService.getUpcoming(userId)));
  }
}
