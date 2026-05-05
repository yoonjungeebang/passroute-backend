package passroutebackend.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.global.jwt.JwtTokenProvider;
import passroutebackend.user.dto.LoginResponse;
import passroutebackend.user.dto.SignUpRequest;
import passroutebackend.user.entity.AuthProvider;
import passroutebackend.user.entity.User;
import passroutebackend.user.repository.UserRepository;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PhoneVerificationService phoneVerificationService;

    @Transactional
    public Long signUp(SignUpRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw CustomException.of(ErrorCode.DUPLICATE_EMAIL);
        }

        phoneVerificationService.checkPhoneVerified(request.getPhone());

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
        phoneVerificationService.deleteVerification(request.getPhone());
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

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.of(ErrorCode.USER_NOT_FOUND));

        if (user.getDeletedAt() != null) {
            throw CustomException.of(ErrorCode.USER_DELETED);
        }

        return issueTokens(userId);
    }

    private LoginResponse issueTokens(Long userId) {
        String accessToken = jwtTokenProvider.generateAccessToken(userId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
        return new LoginResponse(accessToken, refreshToken);
    }
}
