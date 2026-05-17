package passroutebackend.global.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import passroutebackend.global.property.JwtProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    // 테스트용 Base64 시크릿 (최소 32바이트 = 256비트 이상)
    private static final String TEST_SECRET =
            "cGFzc3JvdXRlLWJhY2tlbmQtand0LXNlY3JldC1rZXktZm9yLWRldmVsb3BtZW50LW9ubHk=";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(TEST_SECRET);
        properties.setAccessTokenExpiration(1800000L);   // 30분
        properties.setRefreshTokenExpiration(604800000L); // 7일

        jwtTokenProvider = new JwtTokenProvider(properties);
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("액세스 토큰 생성 후 userId 추출 성공")
    void generateAccessToken_and_getUserId() {
        String token = jwtTokenProvider.generateAccessToken(1L);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(1L);
    }

    @Test
    @DisplayName("리프레시 토큰 생성 후 isRefreshToken 반환 true")
    void generateRefreshToken_isRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken(1L);

        assertThat(jwtTokenProvider.isRefreshToken(token)).isTrue();
    }

    @Test
    @DisplayName("액세스 토큰은 isRefreshToken 반환 false")
    void accessToken_isNotRefreshToken() {
        String token = jwtTokenProvider.generateAccessToken(1L);

        assertThat(jwtTokenProvider.isRefreshToken(token)).isFalse();
    }

    @Test
    @DisplayName("변조된 토큰은 validateToken 반환 false")
    void tamperedToken_validateFails() {
        String token = jwtTokenProvider.generateAccessToken(1L);
        String tampered = token + "hacked";

        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰은 validateToken 반환 false")
    void expiredToken_validateFails() {
        JwtProperties shortLived = new JwtProperties();
        shortLived.setSecret(TEST_SECRET);
        shortLived.setAccessTokenExpiration(1L);  // 1ms - 즉시 만료
        shortLived.setRefreshTokenExpiration(1L);

        JwtTokenProvider shortProvider = new JwtTokenProvider(shortLived);
        shortProvider.init();

        String token = shortProvider.generateAccessToken(1L);

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("빈 문자열은 validateToken 반환 false")
    void emptyToken_validateFails() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
        assertThat(jwtTokenProvider.validateToken("  ")).isFalse();
    }
}
