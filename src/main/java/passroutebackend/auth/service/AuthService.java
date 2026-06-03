package passroutebackend.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.auth.dto.request.SignUpRequest;
import passroutebackend.auth.dto.response.FindEmailResponse;
import passroutebackend.auth.dto.response.LoginResponse;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.global.jwt.JwtTokenProvider;
import passroutebackend.user.entity.AuthProvider;
import passroutebackend.user.entity.User;
import passroutebackend.user.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PhoneVerificationService phoneVerificationService;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public Long signUp(SignUpRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw CustomException.of(ErrorCode.DUPLICATE_EMAIL);
        }

        // TODO: 테스트 단계 - 휴대폰 인증 검증 임시 비활성화
        // phoneVerificationService.checkPhoneVerified(request.getPhone());

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .phoneVerified(true)
                .provider(AuthProvider.LOCAL)
                .experienceYears(request.getExperienceYears())
                .preferredJobTypes(request.getPreferredJobTypes() != null
                        ? new HashSet<>(request.getPreferredJobTypes())
                        : new HashSet<>())
                .preferredCompanies(request.getPreferredCompanies() != null
                        ? new HashSet<>(request.getPreferredCompanies())
                        : new HashSet<>())
                .build();

        Long userId = userRepository.save(user).getId();
        // TODO: 테스트 단계 - 휴대폰 인증 삭제 임시 비활성화
        // phoneVerificationService.deleteVerification(request.getPhone());
        return userId;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> CustomException.of(ErrorCode.USER_NOT_FOUND));

        if (user.getDeletedAt() != null) {
            throw CustomException.of(ErrorCode.USER_DELETED);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw CustomException.of(ErrorCode.INVALID_PASSWORD);
        }

        return issueTokens(user.getId());
    }

    @Transactional(readOnly = true)
    public LoginResponse reissue(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw CustomException.of(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw CustomException.of(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + refreshToken))) {
            throw CustomException.of(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.of(ErrorCode.USER_NOT_FOUND));

        if (user.getDeletedAt() != null) {
            throw CustomException.of(ErrorCode.USER_DELETED);
        }

        return issueTokens(userId);
    }

    @Transactional
    public FindEmailResponse findEmail(String phone, String code) {
        phoneVerificationService.verifyCode(phone, code);
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> CustomException.of(ErrorCode.USER_NOT_FOUND));
        phoneVerificationService.deleteVerification(phone);
        return new FindEmailResponse(maskEmail(user.getEmail()));
    }

    @Transactional
    public void resetPassword(String phone, String code, String newPassword) {
        phoneVerificationService.verifyCode(phone, code);
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> CustomException.of(ErrorCode.USER_NOT_FOUND));
        if (user.getProvider() != AuthProvider.LOCAL) {
            throw CustomException.of(ErrorCode.SOCIAL_USER_NO_PASSWORD);
        }
        user.updatePassword(passwordEncoder.encode(newPassword));
        phoneVerificationService.deleteVerification(phone);
    }

    public void logout(String accessToken, String refreshToken) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        if (!jwtTokenProvider.validateToken(token)) {
            throw CustomException.of(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        long accessExpiration = jwtTokenProvider.getExpiration(token);
        redisTemplate.opsForValue()
                .set("blacklist:" + token, "logout", accessExpiration, TimeUnit.MILLISECONDS);

        if (jwtTokenProvider.validateToken(refreshToken) && jwtTokenProvider.isRefreshToken(refreshToken)) {
            long refreshExpiration = jwtTokenProvider.getExpiration(refreshToken);
            redisTemplate.opsForValue()
                    .set("blacklist:" + refreshToken, "logout", refreshExpiration, TimeUnit.MILLISECONDS);
        }
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email;
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }

    private LoginResponse issueTokens(Long userId) {
        String accessToken = jwtTokenProvider.generateAccessToken(userId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
        return new LoginResponse(accessToken, refreshToken);
    }


}
