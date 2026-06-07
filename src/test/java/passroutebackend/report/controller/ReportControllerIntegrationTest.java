package passroutebackend.report.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.config.S3TestConfig;
import passroutebackend.global.jwt.JwtTokenProvider;
import passroutebackend.interview.client.AiServerClient;
import passroutebackend.interview.dto.report.ItemAvg;
import passroutebackend.interview.dto.report.ItemAverages;
import passroutebackend.interview.entity.Difficulty;
import passroutebackend.interview.entity.InterviewAnswer;
import passroutebackend.interview.entity.InterviewFormat;
import passroutebackend.interview.entity.InterviewQuestion;
import passroutebackend.interview.entity.InterviewReadiness;
import passroutebackend.interview.entity.InterviewReport;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.entity.InterviewType;
import passroutebackend.interview.entity.ReportStatus;
import passroutebackend.interview.entity.RoomStatus;
import passroutebackend.interview.entity.SessionStatus;
import passroutebackend.interview.repository.InterviewAnswerRepository;
import passroutebackend.interview.repository.InterviewQuestionRepository;
import passroutebackend.interview.repository.InterviewReportRepository;
import passroutebackend.interview.repository.InterviewRoomRepository;
import passroutebackend.interview.repository.InterviewSessionRepository;
import passroutebackend.interview.service.EvaluationService;
import passroutebackend.schedule.entity.InterviewSchedule;
import passroutebackend.schedule.entity.ScheduleStatus;
import passroutebackend.schedule.repository.InterviewScheduleRepository;
import passroutebackend.selfintro.dto.SelfIntroRequestDto;
import passroutebackend.selfintro.entity.SelfIntro;
import passroutebackend.selfintro.repository.SelfIntroRepository;
import passroutebackend.user.entity.AuthProvider;
import passroutebackend.user.entity.User;
import passroutebackend.user.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(S3TestConfig.class)
class ReportControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtTokenProvider jwtTokenProvider;
  @Autowired private UserRepository userRepository;
  @Autowired private SelfIntroRepository selfIntroRepository;
  @Autowired private InterviewRoomRepository roomRepository;
  @Autowired private InterviewSessionRepository sessionRepository;
  @Autowired private InterviewReportRepository reportRepository;
  @Autowired private InterviewQuestionRepository questionRepository;
  @Autowired private InterviewAnswerRepository answerRepository;
  @Autowired private InterviewScheduleRepository scheduleRepository;
  @PersistenceContext private EntityManager em;

  @MockBean private AiServerClient aiServerClient;
  @MockBean private EvaluationService evaluationService;
  @MockBean private RedisTemplate<String, String> redisTemplate;

  private User user;
  private User otherUser;
  private String token;
  private String otherToken;
  private SelfIntro selfIntro;

  @BeforeEach
  void setUp() throws Exception {
    user = saveUser("test@test.com", "테스트");
    otherUser = saveUser("other@test.com", "타인");
    token = jwtTokenProvider.generateAccessToken(user.getId());
    otherToken = jwtTokenProvider.generateAccessToken(otherUser.getId());
    selfIntro = saveSelfIntro(user, "카카오", "백엔드");
  }

  // =========================================================
  // GET /api/reports/interview/{sessionId}
  // =========================================================

  @Nested
  @DisplayName("면접 단건 리포트 조회")
  class GetInterviewReport {

    @Test
    @DisplayName("실패 - 인증 토큰 없음 → 401")
    void unauthorized() throws Exception {
      mockMvc.perform(get("/reports/interview/{sessionId}", 1L))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 세션 → 404")
    void notFound() throws Exception {
      mockMvc.perform(get("/reports/interview/{sessionId}", 999999L)
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("I001"));
    }

    @Test
    @DisplayName("성공 - 완료된 리포트 → 200 + body")
    void success() throws Exception {
      InterviewReport report = saveCompletedInterviewReport(selfIntro.getId(), 85.0, 1, List.of());

      mockMvc.perform(get("/reports/interview/{sessionId}", report.getSession().getId())
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("success"))
          .andExpect(jsonPath("$.data.sessionScore").value(85.0));
    }

    @Test
    @DisplayName("실패 - 다른 유저 세션 → 403")
    void accessDenied() throws Exception {
      InterviewReport report = saveCompletedInterviewReport(selfIntro.getId(), 80.0, 1, List.of());

      mockMvc.perform(get("/reports/interview/{sessionId}", report.getSession().getId())
              .header("Authorization", "Bearer " + otherToken))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value("A002"));
    }

    @Test
    @DisplayName("성공 - voice 통계 모두 채워짐 → voiceAnalysis 객체, score는 null")
    void voiceFilled() throws Exception {
      InterviewReport report = saveCompletedInterviewReportWithVoice(
          selfIntro.getId(), 85.0, 1, 142.5, 0.85, 7, null);

      mockMvc.perform(get("/reports/interview/{sessionId}", report.getSession().getId())
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.voiceAnalysis").exists())
          .andExpect(jsonPath("$.data.voiceAnalysis.avgWpm").value(142.5))
          .andExpect(jsonPath("$.data.voiceAnalysis.avgSilenceDuration").value(0.85))
          .andExpect(jsonPath("$.data.voiceAnalysis.fillerCount").value(7))
          .andExpect(jsonPath("$.data.voiceAnalysis.score").doesNotExist());
    }

    @Test
    @DisplayName("성공 - voice 4개 필드 모두 null → voiceAnalysis 자체가 null")
    void voiceAllNull() throws Exception {
      InterviewReport report = saveCompletedInterviewReport(selfIntro.getId(), 80.0, 1, List.of());

      mockMvc.perform(get("/reports/interview/{sessionId}", report.getSession().getId())
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.voiceAnalysis").doesNotExist());
    }

    @Test
    @DisplayName("성공 - voice 부분 데이터 (wpm만 있고 나머지 null) → 그대로 노출")
    void voicePartial() throws Exception {
      InterviewReport report = saveCompletedInterviewReportWithVoice(
          selfIntro.getId(), 80.0, 1, 150.0, null, null, null);

      mockMvc.perform(get("/reports/interview/{sessionId}", report.getSession().getId())
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.voiceAnalysis").exists())
          .andExpect(jsonPath("$.data.voiceAnalysis.avgWpm").value(150.0))
          .andExpect(jsonPath("$.data.voiceAnalysis.avgSilenceDuration").doesNotExist())
          .andExpect(jsonPath("$.data.voiceAnalysis.fillerCount").doesNotExist())
          .andExpect(jsonPath("$.data.voiceAnalysis.score").doesNotExist());
    }

    @Test
    @DisplayName("성공 - 생성 중(GENERATING, 임계값 이내) → 202")
    void generatingFresh() throws Exception {
      InterviewReport report = saveGeneratingInterviewReport(selfIntro.getId(), 1);

      mockMvc.perform(get("/reports/interview/{sessionId}", report.getSession().getId())
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isAccepted());
    }

    @Test
    @DisplayName("실패 - 생성 중이 임계값 초과(stale, 답변 있음) → FAILED 전환 + 500(I010, 재시도 가능)")
    void generatingStale() throws Exception {
      InterviewReport report = saveGeneratingInterviewReport(selfIntro.getId(), 1);
      attachEvaluatedAnswer(report.getSession());  // 답변 있음 → 일시실패(I010)로 분류
      Long sessionId = report.getSession().getId();
      Long reportId = report.getId();
      // created_at을 임계값(기본 480초)보다 과거로 백데이트해 stale 상황 재현
      em.createQuery("UPDATE InterviewReport r SET r.createdAt = :t WHERE r.id = :id")
          .setParameter("t", LocalDateTime.now().minusHours(1))
          .setParameter("id", reportId)
          .executeUpdate();
      em.flush();
      em.clear();

      mockMvc.perform(get("/reports/interview/{sessionId}", sessionId)
              .header("Authorization", "Bearer " + token))
          .andExpect(status().is5xxServerError())
          .andExpect(jsonPath("$.code").value("I010"));

      em.clear();
      InterviewReport reloaded = reportRepository.findById(reportId).orElseThrow();
      org.junit.jupiter.api.Assertions.assertEquals(ReportStatus.FAILED, reloaded.getReportStatus());
    }

    @Test
    @DisplayName("실패 - FAILED + 답변 0개(전부 스킵) → I013(재시도 불가)")
    void failedNoAnswers() throws Exception {
      InterviewReport report = saveInterviewReportWithStatus(selfIntro.getId(), 1, ReportStatus.FAILED);

      mockMvc.perform(get("/reports/interview/{sessionId}", report.getSession().getId())
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(jsonPath("$.code").value("I013"));
    }
  }

  // =========================================================
  // GET /api/reports/debate/{sessionId}
  // =========================================================

  @Nested
  @DisplayName("토론 단건 리포트 조회")
  class GetDebateReport {

    @Test
    @DisplayName("실패 - 인증 토큰 없음 → 401")
    void unauthorized() throws Exception {
      mockMvc.perform(get("/reports/debate/{sessionId}", 1L))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 세션 → 404")
    void notFound() throws Exception {
      mockMvc.perform(get("/reports/debate/{sessionId}", 999999L)
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isNotFound());
    }
  }

  // =========================================================
  // GET /api/reports/self-intro/{selfIntroId}
  // =========================================================

  @Nested
  @DisplayName("자소서 리포트 조회")
  class GetSelfIntroReport {

    @Test
    @DisplayName("실패 - 인증 토큰 없음 → 401")
    void unauthorized() throws Exception {
      mockMvc.perform(get("/reports/self-intro/{id}", selfIntro.getId()))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 자소서 → 404")
    void notFound() throws Exception {
      mockMvc.perform(get("/reports/self-intro/{id}", 999999L)
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("SI001"));
    }

    @Test
    @DisplayName("실패 - 다른 유저 자소서 → 403")
    void accessDenied() throws Exception {
      mockMvc.perform(get("/reports/self-intro/{id}", selfIntro.getId())
              .header("Authorization", "Bearer " + otherToken))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value("A002"));
    }

    @Test
    @DisplayName("실패 - soft-deleted 자소서 → 404")
    void softDeleted() throws Exception {
      selfIntro.softDelete();
      selfIntroRepository.save(selfIntro);

      mockMvc.perform(get("/reports/self-intro/{id}", selfIntro.getId())
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("SI001"));
    }

    @Test
    @DisplayName("성공 - 응시 0건 → 200 + 빈 응답")
    void emptyResponse() throws Exception {
      mockMvc.perform(get("/reports/self-intro/{id}", selfIntro.getId())
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.totalSessions").value(0))
          .andExpect(jsonPath("$.data.hasTrendData").value(false))
          .andExpect(jsonPath("$.data.growthSummary").value("아직 응시 이력이 없습니다. 첫 면접을 시작해보세요."));
    }
  }

  // =========================================================
  // GET /api/reports
  // =========================================================

  @Nested
  @DisplayName("리포트 통합 리스트")
  class GetReports {

    @Test
    @DisplayName("실패 - 인증 토큰 없음 → 401")
    void unauthorized() throws Exception {
      mockMvc.perform(get("/reports"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("성공 - 빈 결과 → 200 + items 빈 배열")
    void empty() throws Exception {
      mockMvc.perform(get("/reports").header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.items.length()").value(0))
          .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    @DisplayName("성공 - 면접 2건 → items 정렬 endedAt DESC")
    void interviewOnly() throws Exception {
      saveCompletedInterviewReport(selfIntro.getId(), 70.0, 1, List.of());
      Thread.sleep(15);
      saveCompletedInterviewReport(selfIntro.getId(), 85.0, 2, List.of());

      mockMvc.perform(get("/reports").header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.items.length()").value(2))
          .andExpect(jsonPath("$.data.items[0].totalScore").value(85.0))
          .andExpect(jsonPath("$.data.items[1].totalScore").value(70.0))
          .andExpect(jsonPath("$.data.items[0].reportType").value("interview"));
    }

    @Test
    @DisplayName("성공 - type=technical 필터 → 면접만, debate 제외")
    void typeFilterTechnical() throws Exception {
      saveCompletedInterviewReport(selfIntro.getId(), 70.0, 1, List.of());

      mockMvc.perform(get("/reports").param("type", "technical").header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.items.length()").value(1));
    }

    @Test
    @DisplayName("성공 - resumeId 지정 → 해당 자소서 면접만 (토론 자동 제외)")
    void resumeIdFilter() throws Exception {
      saveCompletedInterviewReport(selfIntro.getId(), 70.0, 1, List.of());

      mockMvc.perform(get("/reports")
              .param("resumeId", String.valueOf(selfIntro.getId()))
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.items.length()").value(1))
          .andExpect(jsonPath("$.data.items[0].selfIntroId").value(selfIntro.getId()));
    }

    @Test
    @DisplayName("성공 - q 검색 (회사명 부분 일치)")
    void searchByCompanyName() throws Exception {
      saveCompletedInterviewReport(selfIntro.getId(), 70.0, 1, List.of());

      mockMvc.perform(get("/reports").param("q", "카카").header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.items.length()").value(1));
    }

    @Test
    @DisplayName("실패 - 잘못된 type 값 → 400")
    void invalidType() throws Exception {
      mockMvc.perform(get("/reports").param("type", "invalid").header("Authorization", "Bearer " + token))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("G002"));
    }

    @Test
    @DisplayName("실패 - size > 100 → 400")
    void invalidSize() throws Exception {
      mockMvc.perform(get("/reports").param("size", "101").header("Authorization", "Bearer " + token))
          .andExpect(status().isBadRequest());
    }
  }

  // =========================================================
  // GET /api/reports/upcoming
  // =========================================================

  @Nested
  @DisplayName("다가오는 면접 + 이전 회차")
  class GetUpcoming {

    @Test
    @DisplayName("실패 - 인증 토큰 없음 → 401")
    void unauthorized() throws Exception {
      mockMvc.perform(get("/reports/upcoming"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("성공 - 미래 일정 없음 → items 빈 배열")
    void empty() throws Exception {
      mockMvc.perform(get("/reports/upcoming").header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.items.length()").value(0));
    }

    @Test
    @DisplayName("성공 - 과거 일정은 제외, 미래 일정만")
    void futureOnly() throws Exception {
      saveSchedule(user.getId(), "카카오", "백엔드", LocalDateTime.now().minusDays(3));   // past
      saveSchedule(user.getId(), "네이버", "프론트", LocalDateTime.now().plusDays(5));    // future

      mockMvc.perform(get("/reports/upcoming").header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.items.length()").value(1))
          .andExpect(jsonPath("$.data.items[0].companyName").value("네이버"));
    }

    @Test
    @DisplayName("성공 - companyName 매칭으로 이전 회차 채워짐")
    void previousReportsMatching() throws Exception {
      saveCompletedInterviewReport(selfIntro.getId(), 80.0, 1, List.of());  // 카카오
      saveSchedule(user.getId(), "카카오", "백엔드", LocalDateTime.now().plusDays(5));

      mockMvc.perform(get("/reports/upcoming").header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.items.length()").value(1))
          .andExpect(jsonPath("$.data.items[0].previousReports.length()").value(1))
          .andExpect(jsonPath("$.data.items[0].previousReports[0].totalScore").value(80.0))
          .andExpect(jsonPath("$.data.items[0].aiFeedback").doesNotExist());
    }
  }

  // =========================================================
  // Helper
  // =========================================================

  private User saveUser(String email, String name) {
    return userRepository.save(User.builder()
        .email(email)
        .name(name)
        .provider(AuthProvider.LOCAL)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build());
  }

  private SelfIntro saveSelfIntro(User user, String companyName, String jobPosition) throws Exception {
    String json = String.format("""
        { "companyName": "%s", "jobPosition": "%s", "careerLevel": "JUNIOR", "items": [] }
        """, companyName, jobPosition);
    SelfIntroRequestDto dto = objectMapper.readValue(json, SelfIntroRequestDto.class);
    return selfIntroRepository.save(SelfIntro.create(user, dto));
  }

  private InterviewReport saveCompletedInterviewReport(
      Long siId, double score, int sessionNumber, List<String> recommendedQuestions) throws Exception {
    return saveCompletedInterviewReportFull(siId, score, sessionNumber, recommendedQuestions, null, null, null, null);
  }

  private InterviewReport saveCompletedInterviewReportWithVoice(
      Long siId, double score, int sessionNumber,
      Double avgWpm, Double avgSilenceDuration, Integer fillerCount, Double voiceScore) throws Exception {
    return saveCompletedInterviewReportFull(siId, score, sessionNumber, List.of(), avgWpm, avgSilenceDuration, fillerCount, voiceScore);
  }

  private InterviewReport saveCompletedInterviewReportFull(
      Long siId, double score, int sessionNumber, List<String> recommendedQuestions,
      Double avgWpm, Double avgSilenceDuration, Integer fillerCount, Double voiceScore) throws Exception {
    InterviewRoom room = roomRepository.save(InterviewRoom.builder()
        .userId(user.getId())
        .siId(siId)
        .companyName("카카오")
        .jobPosition("백엔드")
        .interviewType(InterviewType.TECHNICAL)
        .interviewFormat(InterviewFormat.ONE_ON_ONE)
        .interviewMode("PRACTICE")
        .aiInterviewer("TECH_INTERVIEWER")
        .interviewCount(3)
        .difficulty(Difficulty.NORMAL)
        .pressureLevel(5)
        .followupCount(3)
        .status(RoomStatus.COMPLETED)
        .build());

    InterviewSession session = sessionRepository.save(InterviewSession.builder()
        .interviewRoom(room)
        .sessionNumber(sessionNumber)
        .status(SessionStatus.IN_PROGRESS)
        .build());
    session.end(SessionStatus.COMPLETED);
    sessionRepository.save(session);

    ItemAverages itemAverages = ItemAverages.builder()
        .logic(new ItemAvg(score / 25.0, 1))
        .build();
    String itemAveragesJson = objectMapper.writeValueAsString(itemAverages);

    InterviewReport saved = reportRepository.save(InterviewReport.builder()
        .session(session)
        .sessionScore(score)
        .interviewReadiness(InterviewReadiness.NEEDS_REVIEW)
        .overall("총평 텍스트")
        .itemAverages(itemAveragesJson)
        .weaknesses(objectMapper.writeValueAsString(List.of()))
        .recommendedQuestions(objectMapper.writeValueAsString(recommendedQuestions))
        .reportStatus(ReportStatus.COMPLETED)
        .avgWpm(avgWpm)
        .avgSilenceDuration(avgSilenceDuration)
        .fillerCount(fillerCount)
        .voiceScore(voiceScore)
        .build());
    em.flush();  // native JdbcTemplate 쿼리에서 즉시 보이도록 DB에 반영
    return saved;
  }

  private InterviewReport saveGeneratingInterviewReport(Long siId, int sessionNumber) {
    return saveInterviewReportWithStatus(siId, sessionNumber, ReportStatus.GENERATING);
  }

  private InterviewReport saveInterviewReportWithStatus(Long siId, int sessionNumber, ReportStatus status) {
    InterviewRoom room = roomRepository.save(InterviewRoom.builder()
        .userId(user.getId())
        .siId(siId)
        .companyName("카카오")
        .jobPosition("백엔드")
        .interviewType(InterviewType.TECHNICAL)
        .interviewFormat(InterviewFormat.ONE_ON_ONE)
        .interviewMode("PRACTICE")
        .aiInterviewer("TECH_INTERVIEWER")
        .interviewCount(3)
        .difficulty(Difficulty.NORMAL)
        .pressureLevel(5)
        .followupCount(3)
        .status(RoomStatus.IN_PROGRESS)
        .build());

    InterviewSession session = sessionRepository.save(InterviewSession.builder()
        .interviewRoom(room)
        .sessionNumber(sessionNumber)
        .status(SessionStatus.IN_PROGRESS)
        .build());
    session.end(SessionStatus.COMPLETED);  // 리포트 생성은 세션 종료 후 시작되므로 종료 상태여야 함
    sessionRepository.save(session);

    InterviewReport saved = reportRepository.save(InterviewReport.builder()
        .session(session)
        .reportStatus(status)
        .build());
    em.flush();
    return saved;
  }

  // 평가 완료(percentage 채워진) 답변 1건을 세션에 부착 → "답변 있음"(일시실패 I010) 케이스 구성
  private void attachEvaluatedAnswer(InterviewSession session) {
    InterviewQuestion q = questionRepository.save(InterviewQuestion.builder()
        .session(session)
        .setNumber(1)
        .questionText("질문")
        .questionOrder(1)
        .followUp(false)
        .build());
    InterviewAnswer a = InterviewAnswer.builder().question(q).answerText("답변").build();
    a.updateEvaluationResult(80.0, 3, null, 4.0);
    answerRepository.save(a);
    em.flush();
  }

  private InterviewSchedule saveSchedule(Long userId, String companyName, String jobPosition, LocalDateTime when) {
    InterviewSchedule saved = scheduleRepository.save(InterviewSchedule.builder()
        .userId(userId)
        .title(companyName + " 면접")
        .companyName(companyName)
        .jobPosition(jobPosition)
        .interviewDate(when)
        .status(ScheduleStatus.SCHEDULED)
        .build());
    em.flush();
    return saved;
  }
}
