package passroutebackend.report.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.report.dto.response.ReportListItem;
import passroutebackend.report.dto.response.ReportListResponse;
import passroutebackend.report.repository.ReportListQueryParams;
import passroutebackend.report.repository.ReportListQueryRepository;

@Service
@RequiredArgsConstructor
public class ReportListService {

  public static final int MAX_PAGE_SIZE = 100;
  public static final int DEFAULT_PAGE_SIZE = 20;

  private final ReportListQueryRepository queryRepository;

  @Transactional(readOnly = true)
  public ReportListResponse getReports(
      Long userId, Long resumeId, String type, String q, int page, int size) {

    validatePaging(page, size);
    ReportListQueryParams params = buildParams(userId, resumeId, type, q);

    long total = queryRepository.count(params);
    List<ReportListItem> items = total > 0
        ? queryRepository.findReports(params, page, size)
        : List.of();

    return ReportListResponse.builder()
        .items(items)
        .page(page)
        .size(size)
        .total(total)
        .build();
  }

  private void validatePaging(int page, int size) {
    if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
      throw CustomException.of(ErrorCode.INVALID_INPUT);
    }
  }

  private ReportListQueryParams buildParams(Long userId, Long resumeId, String type, String q) {
    boolean includeInterview;
    boolean includeDebate;
    String interviewTypeFilter;

    String normalizedType = (type == null || type.isBlank()) ? "all" : type.trim().toLowerCase();
    switch (normalizedType) {
      case "all" -> {
        includeInterview = true;
        includeDebate = true;
        interviewTypeFilter = null;
      }
      case "technical" -> {
        includeInterview = true;
        includeDebate = false;
        interviewTypeFilter = "TECHNICAL";
      }
      case "personality" -> {
        includeInterview = true;
        includeDebate = false;
        interviewTypeFilter = "PERSONALITY";
      }
      case "debate" -> {
        includeInterview = false;
        includeDebate = true;
        interviewTypeFilter = null;
      }
      default -> throw CustomException.of(ErrorCode.INVALID_INPUT);
    }

    // resumeId가 지정되면 토론은 자동 제외 (구조상 자소서와 연결되지 않음)
    if (resumeId != null) {
      includeDebate = false;
    }

    return new ReportListQueryParams(
        userId,
        includeInterview,
        includeDebate,
        interviewTypeFilter,
        resumeId,
        normalizeSearchTerm(q)
    );
  }

  private String normalizeSearchTerm(String q) {
    if (q == null) return null;
    String trimmed = q.trim();
    if (trimmed.isEmpty()) return null;
    return "%" + trimmed.toLowerCase() + "%";
  }
}
