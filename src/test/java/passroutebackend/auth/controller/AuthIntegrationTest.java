package passroutebackend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import passroutebackend.auth.entity.PhoneVerification;
import passroutebackend.auth.repository.PhoneVerificationRepository;
import passroutebackend.auth.dto.request.LoginRequest;
import passroutebackend.auth.dto.request.SignUpRequest;
import passroutebackend.global.config.S3TestConfig;
import passroutebackend.global.sms.SmsService;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(S3TestConfig.class)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PhoneVerificationRepository phoneVerificationRepository;

    @MockBean
    private SmsService smsService;

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    private static final String SIGNUP_URL = "/auth/signup";
    private static final String LOGIN_URL  = "/auth/login";
    private static final String REISSUE_URL = "/auth/reissue";

    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() throws Exception {
        preVerifyPhone("01011111111");
        SignUpRequest request = createSignUpRequest("test@example.com", "password123", "테스트유저", "01011111111");

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("회원가입 성공 - 선택 정보 포함 (연차, 선호직군, 선호회사)")
    void signUp_success_withProfile() throws Exception {
        preVerifyPhone("01022222222");
        String json = """
                {
                  "email": "profile@example.com",
                  "password": "password123",
                  "name": "프로필유저",
                  "phone": "01022222222",
                  "experienceYears": 3,
                  "preferredJobTypes": ["BACKEND_DEVELOPER", "FULLSTACK_DEVELOPER"],
                  "preferredCompanies": ["카카오", "토스"]
                }
                """;

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 형식 오류")
    void signUp_fail_invalidEmail() throws Exception {
        SignUpRequest request = createSignUpRequest("not-an-email", "password123", "테스트유저", "01033333333");

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 8자 미만")
    void signUp_fail_shortPassword() throws Exception {
        SignUpRequest request = createSignUpRequest("test@example.com", "short", "테스트유저", "01044444444");

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 이메일")
    void signUp_fail_duplicateEmail() throws Exception {
        preVerifyPhone("01055555555");
        SignUpRequest request = createSignUpRequest("dup@example.com", "password123", "테스트유저", "01055555555");

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        // 같은 이메일 두 번째 시도 (이메일 체크가 먼저 실행됨)
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("U003"));
    }

    @Test
    @DisplayName("회원가입 실패 - 휴대폰 미인증")
    void signUp_fail_phoneNotVerified() throws Exception {
        SignUpRequest request = createSignUpRequest("unverified@example.com", "password123", "미인증유저", "01066666666");

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("P004"));
    }

    @Test
    @DisplayName("로그인 성공 - accessToken, refreshToken 발급")
    void login_success() throws Exception {
        preVerifyPhone("01077777777");
        SignUpRequest signUpRequest = createSignUpRequest("login@example.com", "password123", "로그인유저", "01077777777");
        mockMvc.perform(post(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));

        LoginRequest loginRequest = createLoginRequest("login@example.com", "password123");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_fail_userNotFound() throws Exception {
        LoginRequest request = createLoginRequest("nobody@example.com", "password123");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("U001"));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_wrongPassword() throws Exception {
        preVerifyPhone("01088888888");
        SignUpRequest signUpRequest = createSignUpRequest("pw@example.com", "password123", "유저", "01088888888");
        mockMvc.perform(post(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));

        LoginRequest loginRequest = createLoginRequest("pw@example.com", "wrongpassword");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A006"));
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissue_success() throws Exception {
        preVerifyPhone("01099999999");
        SignUpRequest signUpRequest = createSignUpRequest("reissue@example.com", "password123", "재발급유저", "01099999999");
        mockMvc.perform(post(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));

        String loginResponse = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                createLoginRequest("reissue@example.com", "password123"))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse)
                .path("data").path("refreshToken").asText();

        mockMvc.perform(post(REISSUE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("보호된 API 토큰 없이 접근 시 401 JSON 반환")
    void protectedApi_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/some-protected-endpoint"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.code").value("A001"));
    }

    private void preVerifyPhone(String phone) {
        PhoneVerification verification = PhoneVerification.builder()
                .phone(phone)
                .code("123456")
                .verified(true)
                .verifiedAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(3))
                .createdAt(LocalDateTime.now())
                .build();
        phoneVerificationRepository.save(verification);
    }

    private SignUpRequest createSignUpRequest(String email, String password, String name, String phone) {
        try {
            String json = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\",\"name\":\"%s\",\"phone\":\"%s\"}",
                    email, password, name, phone);
            return objectMapper.readValue(json, SignUpRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private LoginRequest createLoginRequest(String email, String password) {
        try {
            String json = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
            return objectMapper.readValue(json, LoginRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
