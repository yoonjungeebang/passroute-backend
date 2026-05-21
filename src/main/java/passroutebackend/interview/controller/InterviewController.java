package passroutebackend.interview.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import passroutebackend.global.ApiResponse;
import passroutebackend.interview.dto.report.InterviewReportResponse;
import passroutebackend.interview.service.ReportService;
import passroutebackend.interview.service.ReportTransactionService;

import java.util.Optional;

@RestController
@RequestMapping("/interviews")
@RequiredArgsConstructor
public class InterviewController {

  private final ReportTransactionService reportTransactionService;
  private final ReportService reportService;

  @PostMapping("/{sessionId}/end")
  public ResponseEntity<ApiResponse<Void>> endSession(@PathVariable Long sessionId) {
    boolean ended = reportTransactionService.endSession(sessionId);
    if (ended) {
      reportService.generateReportAsync(sessionId);
    }
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(ApiResponse.accepted("리포트를 생성 중입니다."));
  }

  @GetMapping("/{sessionId}/report")
  public ResponseEntity<ApiResponse<InterviewReportResponse>> getReport(@PathVariable Long sessionId) {
    Optional<InterviewReportResponse> report = reportService.getReport(sessionId);
    if (report.isEmpty()) {
      return ResponseEntity.status(HttpStatus.ACCEPTED)
          .body(ApiResponse.accepted("리포트 생성 중입니다."));
    }
    return ResponseEntity.ok(ApiResponse.success(report.get()));
  }
}
