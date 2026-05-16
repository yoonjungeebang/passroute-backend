package passroutebackend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.user.dto.LoginRequest;
import passroutebackend.user.dto.SignUpRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // context-path가 /api 이므로 MockMvc에선 경로만 사용
    private static final String SIGNUP_URL = "/auth/signup";
    private static final String LOGIN_URL  = "/auth/login";
    private static final String REISSUE_URL = "/auth/reissue";

    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() throws Exception {
        SignUpRequest request = createSignUpRequest("test@example.com", "password123", "테스트유저");

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 형식 오류")
    void signUp_fail_invalidEmail() throws Exception {
        SignUpRequest request = createSignUpRequest("not-an-email", "password123", "테스트유저");

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 8자 미만")
    void signUp_fail_shortPassword() throws Exception {
        SignUpRequest request = createSignUpRequest("test@example.com", "short", "테스트유저");

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 이메일")
    void signUp_fail_duplicateEmail() throws Exception {
        SignUpRequest request = createSignUpRequest("dup@example.com", "password123", "테스트유저");

        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        // 같은 이메일 두 번째 시도
        mockMvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("U003"));
    }

    @Test
    @DisplayName("로그인 성공 - accessToken, refreshToken 발급")
    void login_success() throws Exception {
        // 회원가입 먼저
        SignUpRequest signUpRequest = createSignUpRequest("login@example.com", "password123", "로그인유저");
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
        SignUpRequest signUpRequest = createSignUpRequest("pw@example.com", "password123", "유저");
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
        // 로그인해서 refreshToken 획득
        SignUpRequest signUpRequest = createSignUpRequest("reissue@example.com", "password123", "재발급유저");
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

    private SignUpRequest createSignUpRequest(String email, String password, String name) {
        try {
            String json = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\",\"name\":\"%s\"}",
                    email, password, name);
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
