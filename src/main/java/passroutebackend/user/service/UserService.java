package passroutebackend.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.global.jwt.JwtTokenProvider;
import passroutebackend.user.entity.AuthProvider;
import passroutebackend.user.entity.User;
import passroutebackend.user.repository.UserRepository;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void withdraw(Long userId, String accessToken, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.of(ErrorCode.USER_NOT_FOUND));

        if (user.getDeletedAt() != null) {
            throw CustomException.of(ErrorCode.USER_DELETED);
        }

        if (user.getProvider() == AuthProvider.LOCAL) {
            if (password == null || !passwordEncoder.matches(password, user.getPassword())) {
                throw CustomException.of(ErrorCode.INVALID_PASSWORD);
            }
        }

        user.softDelete();

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        long expiration = jwtTokenProvider.getExpiration(token);
        redisTemplate.opsForValue().set("blacklist:" + token, "withdraw", expiration, TimeUnit.MILLISECONDS);
    }
}
