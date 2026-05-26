package passroutebackend.selfintro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import passroutebackend.interview.entity.InterviewFormat;
import passroutebackend.interview.entity.InterviewReadiness;
import passroutebackend.interview.entity.InterviewReport;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.entity.InterviewType;
import passroutebackend.interview.entity.ReportStatus;
import passroutebackend.interview.entity.RoomStatus;
import passroutebackend.interview.entity.SessionStatus;
import passroutebackend.interview.repository.InterviewReportRepository;
import passroutebackend.interview.repository.InterviewRoomRepository;
import passroutebackend.interview.repository.InterviewSessionRepository;
import passroutebackend.interview.service.EvaluationService;
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
class SelfIntroReportIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtTokenProvider jwtTokenProvider;
  @Autowired private UserRepository userRepository;
  @Autowired private SelfIntroRepository selfIntroRepository;
  @Autowired private InterviewRoomRepository roomRepository;
  @Autowired private InterviewSessionRepository sessionRepository;
  @Autowired private InterviewReportRepository reportRepository;

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
  // GET /self-intro/{id}/report
  // =========================================================

  @Nested
  @DisplayName("자소서 리포트 조회 - 권한 및 존재 검증")
  class AccessAndExistence {

    @Test
    @DisplayName("실패 - 인증 토큰 없음 → 401")
    void unauthorized() throws Exception {
      mockMvc.perform(get("/self-intro/{id}/report", selfIntro.getId()))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 자소서 → 404")
    void selfIntroNotFound() throws Exception {
      mockMvc.perform(get("/self-intro/{id}/report", 999999L)
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("SI001"));
    }

    @Test
    @DisplayName("실패 - 다른 유저의 자소서 → 403")
    void accessDenied() throws Exception {
      mockMvc.perform(get("/self-intro/{id}/report", selfIntro.getId())
              .header("Authorization", "Bearer " + otherToken))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value("A002"));
    }

    @Test
    @DisplayName("실패 - soft-deleted 자소서 → 404")
    void softDeleted() throws Exception {
      selfIntro.softDelete();
      selfIntroRepository.save(selfIntro);

      mockMvc.perform(get("/self-intro/{id}/report", selfIntro.getId())
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("SI001"));
    }
  }

  @Nested
  @DisplayName("자소서 리포트 조회 - N=0 (응시 0건)")
  class EmptyResponse {

    @Test
    @DisplayName("성공 - 200 + 빈 응답 + N=0 안내 메시지")
    void success() throws Exception {
      mockMvc.perform(get("/self-intro/{id}/report", selfIntro.getId())
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.selfIntroId").value(selfIntro.getId()))
          .andExpect(jsonPath("$.companyName").value("카카오"))
          .andExpect(jsonPath("$.jobPosition").value("백엔드"))
          .andExpect(jsonPath("$.totalSessions").value(0))
          .andExpect(jsonPath("$.hasTrendData").value(false))
          .andExpect(jsonPath("$.scoreTimeline.length()").value(0))
          .andExpect(jsonPath("$.overallAverage").doesNotExist())
          .andExpect(jsonPath("$.itemAverages").doesNotExist())
          .andExpect(jsonPath("$.itemTrend.length()").value(0))
          .andExpect(jsonPath("$.bestSession").doesNotExist())
          .andExpect(jsonPath("$.worstSession").doesNotExist())
          .andExpect(jsonPath("$.topRecommendedQuestions.length()").value(0))
          .andExpect(jsonPath("$.readiness").doesNotExist())
          .andExpect(jsonPath("$.growthSummary").value("아직 응시 이력이 없습니다. 첫 면접을 시작해보세요."));
    }
  }

  @Nested
  @DisplayName("자소서 리포트 조회 - N=1 (응시 1건)")
  class SingleSession {

    @Test
    @DisplayName("성공 - hasTrendData false, itemTrend 빈 배열, N=1 메시지")
    void success() throws Exception {
      saveCompletedReport(selfIntro.getId(), 70.0, 1, List.of("질문1", "질문2"));

      mockMvc.perform(get("/self-intro/{id}/report", selfIntro.getId())
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalSessions").value(1))
          .andExpect(jsonPath("$.hasTrendData").value(false))
          .andExpect(jsonPath("$.scoreTimeline.length()").value(1))
          .andExpect(jsonPath("$.scoreTimeline[0].round").value(1))
          .andExpect(jsonPath("$.scoreTimeline[0].score").value(70.0))
          .andExpect(jsonPath("$.overallAverage").value(70.0))
          .andExpect(jsonPath("$.itemTrend.length()").value(0))
          .andExpect(jsonPath("$.bestSession.round").value(1))
          .andExpect(jsonPath("$.worstSession.round").value(1))
          .andExpect(jsonPath("$.readiness.level").value("NEEDS_REVIEW"))
          .andExpect(jsonPath("$.growthSummary").value("1회 응시했습니다. 회차별 추이 분석은 2회차부터 가능합니다."));
    }
  }

  @Nested
  @DisplayName("자소서 리포트 조회 - N>=2 (응시 다수)")
  class MultipleSessions {

    @Test
    @DisplayName("성공 - 3회차 (58/72/85) → 성장세, hasTrendData true, itemTrend 채워짐")
    void growthUp() throws Exception {
      saveCompletedReport(selfIntro.getId(), 58.0, 1, List.of("프로젝트 성능", "GC 튜닝"));
      Thread.sleep(15);
      saveCompletedReport(selfIntro.getId(), 72.0, 2, List.of("프로젝트 성능", "동시성"));
      Thread.sleep(15);
      saveCompletedReport(selfIntro.getId(), 85.0, 3, List.of("프로젝트 성능", "DB 인덱스"));

      mockMvc.perform(get("/self-intro/{id}/report", selfIntro.getId())
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalSessions").value(3))
          .andExpect(jsonPath("$.hasTrendData").value(true))
          .andExpect(jsonPath("$.scoreTimeline.length()").value(3))
          .andExpect(jsonPath("$.scoreTimeline[0].score").value(58.0))
          .andExpect(jsonPath("$.scoreTimeline[2].score").value(85.0))
          .andExpect(jsonPath("$.bestSession.round").value(3))
          .andExpect(jsonPath("$.bestSession.score").value(85.0))
          .andExpect(jsonPath("$.worstSession.round").value(1))
          .andExpect(jsonPath("$.worstSession.score").value(58.0))
          .andExpect(jsonPath("$.itemTrend.length()").value(org.hamcrest.Matchers.greaterThan(0)))
          .andExpect(jsonPath("$.readiness.level").value("NEEDS_REVIEW"))
          .andExpect(jsonPath("$.topRecommendedQuestions[0].text").value("프로젝트 성능"))
          .andExpect(jsonPath("$.topRecommendedQuestions[0].count").value(3))
          .andExpect(jsonPath("$.growthSummary").value(org.hamcrest.Matchers.containsString("향상")));
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
        {
          "companyName": "%s",
          "jobPosition": "%s",
          "careerLevel": "JUNIOR",
          "items": []
        }
        """, companyName, jobPosition);
    SelfIntroRequestDto dto = objectMapper.readValue(json, SelfIntroRequestDto.class);
    return selfIntroRepository.save(SelfIntro.create(user, dto));
  }

  private InterviewReport saveCompletedReport(
      Long siId, double score, int sessionNumber, List<String> recommendedQuestions) throws Exception {
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

    // 회차마다 점수를 살짝 다르게 줘서 itemTrend 변화 확인 가능하게
    double itemBase = score / 25.0; // 0~4 스케일로 환산
    ItemAverages itemAverages = ItemAverages.builder()
        .logic(new ItemAvg(itemBase, 1))
        .specificity(new ItemAvg(itemBase, 1))
        .build();
    String itemAveragesJson = objectMapper.writeValueAsString(itemAverages);
    String recommendedJson = objectMapper.writeValueAsString(recommendedQuestions);
    String weaknessesJson = objectMapper.writeValueAsString(List.of());

    return reportRepository.save(InterviewReport.builder()
        .session(session)
        .sessionScore(score)
        .interviewReadiness(InterviewReadiness.NEEDS_REVIEW)
        .itemAverages(itemAveragesJson)
        .weaknesses(weaknessesJson)
        .recommendedQuestions(recommendedJson)
        .reportStatus(ReportStatus.COMPLETED)
        .build());
  }
}
