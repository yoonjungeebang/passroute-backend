package passroutebackend.interview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.config.S3TestConfig;
import passroutebackend.global.jwt.JwtTokenProvider;
import passroutebackend.interview.client.AiServerClient;
import passroutebackend.interview.dto.FollowUpResponse;
import passroutebackend.interview.entity.Difficulty;
import passroutebackend.interview.entity.InterviewAnswer;
import passroutebackend.interview.entity.InterviewFormat;
import passroutebackend.interview.entity.InterviewQuestion;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.entity.InterviewType;
import passroutebackend.interview.entity.RoomStatus;
import passroutebackend.interview.entity.SessionStatus;
import passroutebackend.interview.repository.InterviewAnswerRepository;
import passroutebackend.interview.repository.InterviewQuestionRepository;
import passroutebackend.interview.repository.InterviewRoomRepository;
import passroutebackend.interview.repository.InterviewSessionRepository;
import passroutebackend.interview.service.EvaluationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(S3TestConfig.class)
class InterviewProgressIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtTokenProvider jwtTokenProvider;
  @Autowired private InterviewRoomRepository roomRepository;
  @Autowired private InterviewSessionRepository sessionRepository;
  @Autowired private InterviewQuestionRepository questionRepository;
  @Autowired private InterviewAnswerRepository answerRepository;

  @MockBean private AiServerClient aiServerClient;
  @MockBean private EvaluationService evaluationService; // @Async 사이드이펙트 차단
  @MockBean private RedisTemplate<String, String> redisTemplate;

  private static final Long USER_ID = 1L;
  private static final Long OTHER_USER_ID = 99L;

  // 스킵 키워드 포함 → 꼬리질문 생성 안 함
  private static final String SKIP_QUESTION_TEXT = "자기소개를 해주세요.";
  // 스킵 키워드 없음 → 꼬리질문 생성 시도
  private static final String NORMAL_QUESTION_TEXT = "객체지향의 특징을 설명해주세요.";

  private String token;
  private InterviewRoom room;
  private InterviewSession session;
  private InterviewQuestion q1; // setNumber=1, 스킵 키워드 질문
  private InterviewQuestion q2; // setNumber=2, 일반 질문

  @BeforeEach
  void setUp() {
    token = jwtTokenProvider.generateAccessToken(USER_ID);

    // 기본 mock: 꼬리질문 없음
    Mockito.when(aiServerClient.requestFollowUp(any()))
        .thenReturn(FollowUpResponse.noFollowUp());
    Mockito.doNothing().when(evaluationService).evaluateAsync(anyLong(), any());

    room = roomRepository.save(InterviewRoom.builder()
        .userId(USER_ID)
        .interviewType(InterviewType.PERSONALITY)
        .interviewFormat(InterviewFormat.ONE_ON_ONE)
        .interviewMode("PRACTICE")
        .interviewCount(2)
        .difficulty(Difficulty.NORMAL)
        .pressureLevel(1)
        .followupCount(1)
        .status(RoomStatus.IN_PROGRESS)
        .build());

    session = sessionRepository.save(InterviewSession.builder()
        .interviewRoom(room)
        .sessionNumber(1)
        .status(SessionStatus.IN_PROGRESS)
        .build());

    q1 = questionRepository.save(InterviewQuestion.builder()
        .session(session)
        .setNumber(1)
        .questionText(SKIP_QUESTION_TEXT)
        .questionOrder(1)
        .followUp(false)
        .build());

    q2 = questionRepository.save(InterviewQuestion.builder()
        .session(session)
        .setNumber(2)
        .questionText(NORMAL_QUESTION_TEXT)
        .questionOrder(2)
        .followUp(false)
        .build());
  }

  // =========================================================
  // GET /interview/sessions/{sessionId}/questions
  // =========================================================

  @Nested
  @DisplayName("질문 목록 조회")
  class GetQuestions {

    @Test
    @DisplayName("성공 - 질문 목록을 questionOrder 오름차순으로 반환")
    void success() throws Exception {
      mockMvc.perform(get("/interview/sessions/{sessionId}/questions", session.getId())
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("success"))
          .andExpect(jsonPath("$.data.questions.length()").value(2))
          .andExpect(jsonPath("$.data.questions[0].questionId").value(q1.getId()))
          .andExpect(jsonPath("$.data.questions[0].questionText").value(SKIP_QUESTION_TEXT))
          .andExpect(jsonPath("$.data.questions[0].questionOrder").value(1))
          .andExpect(jsonPath("$.data.questions[1].questionId").value(q2.getId()))
          .andExpect(jsonPath("$.data.questions[1].questionOrder").value(2));
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 세션 → 404")
    void sessionNotFound() throws Exception {
      mockMvc.perform(get("/interview/sessions/{sessionId}/questions", 999999L)
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.status").value("error"))
          .andExpect(jsonPath("$.code").value("I001"));
    }

    @Test
    @DisplayName("실패 - 다른 유저의 세션 접근 → 403")
    void accessDenied() throws Exception {
      String otherToken = jwtTokenProvider.generateAccessToken(OTHER_USER_ID);

      mockMvc.perform(get("/interview/sessions/{sessionId}/questions", session.getId())
              .header("Authorization", "Bearer " + otherToken))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value("A002"));
    }

    @Test
    @DisplayName("실패 - 인증 토큰 없음 → 401")
    void unauthorized() throws Exception {
      mockMvc.perform(get("/interview/sessions/{sessionId}/questions", session.getId()))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.code").value("A001"));
    }
  }

  // =========================================================
  // POST /interview/sessions/{sessionId}/answers
  // =========================================================

  @Nested
  @DisplayName("답변 제출")
  class SubmitAnswer {

    @Test
    @DisplayName("성공 - 꼬리질문 없음, 아직 마지막 질문 아님")
    void noFollowUp_notLast() throws Exception {
      // q1만 답변 → 2개 중 1개 → lastQuestion = false
      String body = answerBody(q1.getId(), "저는 백엔드 개발자입니다.");

      mockMvc.perform(post("/interview/sessions/{sessionId}/answers", session.getId())
              .header("Authorization", "Bearer " + token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.hasFollowUp").value(false))
          .andExpect(jsonPath("$.data.followUpQuestionId").isEmpty())
          .andExpect(jsonPath("$.data.lastQuestion").value(false));
    }

    @Test
    @DisplayName("성공 - 꼬리질문 생성됨 → lastQuestion은 무조건 false")
    void withFollowUp() throws Exception {
      // AI 서버가 꼬리질문 반환하도록 mock 설정
      Mockito.when(aiServerClient.requestFollowUp(any()))
          .thenReturn(new FollowUpResponse(true, "구체적인 경험을 말씀해주세요.", null, null));

      // q2 (스킵 키워드 없는 질문) 답변
      String body = answerBody(q2.getId(), "캡슐화, 상속, 다형성, 추상화가 있습니다.");

      mockMvc.perform(post("/interview/sessions/{sessionId}/answers", session.getId())
              .header("Authorization", "Bearer " + token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.hasFollowUp").value(true))
          .andExpect(jsonPath("$.data.followUpQuestionId").isNotEmpty())
          .andExpect(jsonPath("$.data.followUpQuestionText").value("구체적인 경험을 말씀해주세요."))
          .andExpect(jsonPath("$.data.lastQuestion").value(false));
    }

    @Test
    @DisplayName("성공 - 모든 질문에 답변 완료 → lastQuestion = true")
    void lastQuestion() throws Exception {
      // q1에 먼저 답변 저장 (사전 조건)
      answerRepository.save(InterviewAnswer.builder()
          .question(q1)
          .answerText("사전 저장된 답변")
          .build());

      // q2 답변 → 2/2 완료 + 꼬리질문 없음 → lastQuestion = true
      String body = answerBody(q2.getId(), "객체지향의 마지막 답변입니다.");

      mockMvc.perform(post("/interview/sessions/{sessionId}/answers", session.getId())
              .header("Authorization", "Bearer " + token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.hasFollowUp").value(false))
          .andExpect(jsonPath("$.data.lastQuestion").value(true));
    }

    @Test
    @DisplayName("실패 - 이미 종료된 세션 → 400")
    void alreadyEndedSession() throws Exception {
      session.end(SessionStatus.COMPLETED);

      String body = answerBody(q1.getId(), "답변 내용");

      mockMvc.perform(post("/interview/sessions/{sessionId}/answers", session.getId())
              .header("Authorization", "Bearer " + token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("I003"));
    }

    @Test
    @DisplayName("실패 - questionId 누락 → 400")
    void missingQuestionId() throws Exception {
      String body = """
          {"answerText": "답변 내용"}
          """;

      mockMvc.perform(post("/interview/sessions/{sessionId}/answers", session.getId())
              .header("Authorization", "Bearer " + token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패 - answerText 빈 값 → 400")
    void blankAnswerText() throws Exception {
      String body = answerBody(q1.getId(), "   ");

      mockMvc.perform(post("/interview/sessions/{sessionId}/answers", session.getId())
              .header("Authorization", "Bearer " + token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패 - 다른 유저의 세션 접근 → 403")
    void accessDenied() throws Exception {
      String otherToken = jwtTokenProvider.generateAccessToken(OTHER_USER_ID);
      String body = answerBody(q1.getId(), "답변 내용");

      mockMvc.perform(post("/interview/sessions/{sessionId}/answers", session.getId())
              .header("Authorization", "Bearer " + otherToken)
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value("A002"));
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 questionId → 404")
    void questionNotFound() throws Exception {
      String body = answerBody(999999L, "답변 내용");

      mockMvc.perform(post("/interview/sessions/{sessionId}/answers", session.getId())
              .header("Authorization", "Bearer " + token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("I002"));
    }
  }

  // =========================================================
  // POST /interview/sessions/{sessionId}/end
  // =========================================================

  @Nested
  @DisplayName("면접 종료")
  class EndSession {

    @Test
    @DisplayName("성공 - 세션·룸 상태가 COMPLETED로 변경되고 리포트 생성 요청이 수락됨")
    void success() throws Exception {
      mockMvc.perform(post("/interview/sessions/{sessionId}/end", session.getId())
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isAccepted())
          .andExpect(jsonPath("$.status").value("success"))
          .andExpect(jsonPath("$.code").value("ACCEPTED"))
          .andExpect(jsonPath("$.data").isEmpty());

      // DB 상태 직접 검증
      InterviewSession updated = sessionRepository.findById(session.getId()).orElseThrow();
      assert updated.getStatus() == SessionStatus.COMPLETED;
      assert updated.getEndedAt() != null;

      InterviewRoom updatedRoom = roomRepository.findById(room.getId()).orElseThrow();
      assert updatedRoom.getStatus() == RoomStatus.COMPLETED;
    }

    @Test
    @DisplayName("실패 - 이미 종료된 세션 → 400")
    void alreadyEnded() throws Exception {
      session.end(SessionStatus.COMPLETED);

      mockMvc.perform(post("/interview/sessions/{sessionId}/end", session.getId())
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("I003"));
    }

    @Test
    @DisplayName("실패 - 다른 유저의 세션 접근 → 403")
    void accessDenied() throws Exception {
      String otherToken = jwtTokenProvider.generateAccessToken(OTHER_USER_ID);

      mockMvc.perform(post("/interview/sessions/{sessionId}/end", session.getId())
              .header("Authorization", "Bearer " + otherToken))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value("A002"));
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 세션 → 404")
    void sessionNotFound() throws Exception {
      mockMvc.perform(post("/interview/sessions/{sessionId}/end", 999999L)
              .header("Authorization", "Bearer " + token))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("I001"));
    }

    @Test
    @DisplayName("실패 - 인증 토큰 없음 → 401")
    void unauthorized() throws Exception {
      mockMvc.perform(post("/interview/sessions/{sessionId}/end", session.getId()))
          .andExpect(status().isUnauthorized());
    }
  }

  // GET /api/reports/interview/{sessionId} 테스트는 ReportControllerIntegrationTest로 이전

  // =========================================================
  // Helper
  // =========================================================

  private String answerBody(Long questionId, String answerText) {
    return String.format("{\"questionId\":%d,\"answerText\":\"%s\"}", questionId, answerText);
  }
}
