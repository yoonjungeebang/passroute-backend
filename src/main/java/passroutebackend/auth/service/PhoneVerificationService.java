package passroutebackend.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.auth.entity.PhoneVerification;
import passroutebackend.auth.repository.PhoneVerificationRepository;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.global.sms.SmsService;
import passroutebackend.user.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int CODE_VALIDITY_MINUTES = 3;
    private static final int VERIFIED_VALIDITY_MINUTES = 30;

    private final PhoneVerificationRepository phoneVerificationRepository;
    private final UserRepository userRepository;
    private final SmsService smsService;

    @Transactional
    public void sendVerificationCode(String phone) {
        if (userRepository.findByPhone(phone).isPresent()) {
            throw CustomException.of(ErrorCode.DUPLICATE_PHONE);
        }
        sendCode(phone);
    }

    @Transactional
    public void sendCodeForRegisteredPhone(String phone) {
        userRepository.findByPhone(phone)
                .orElseThrow(() -> CustomException.of(ErrorCode.PHONE_NOT_REGISTERED));
        sendCode(phone);
    }

    private void sendCode(String phone) {

        phoneVerificationRepository.deleteByPhone(phone);

        String code = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));

        PhoneVerification verification = PhoneVerification.builder()
                .phone(phone)
                .code(code)
                .verified(false)
                .expiredAt(LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES))
                .createdAt(LocalDateTime.now())
                .build();

        phoneVerificationRepository.save(verification);
        smsService.sendSms(phone, "[패스루트] 인증번호: " + code);
    }

    @Transactional
    public void verifyCode(String phone, String code) {
        PhoneVerification verification = phoneVerificationRepository
                .findTopByPhoneOrderByCreatedAtDesc(phone)
                .orElseThrow(() -> CustomException.of(ErrorCode.PHONE_VERIFICATION_NOT_FOUND));

        if (verification.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw CustomException.of(ErrorCode.PHONE_VERIFICATION_EXPIRED);
        }

        if (!verification.getCode().equals(code)) {
            throw CustomException.of(ErrorCode.PHONE_VERIFICATION_INVALID_CODE);
        }

        verification.verify();
    }

    @Transactional(readOnly = true)
    public void checkPhoneVerified(String phone) {
        PhoneVerification verification = phoneVerificationRepository
                .findTopByPhoneOrderByCreatedAtDesc(phone)
                .orElseThrow(() -> CustomException.of(ErrorCode.PHONE_NOT_VERIFIED));

        if (!verification.isVerified()) {
            throw CustomException.of(ErrorCode.PHONE_NOT_VERIFIED);
        }

        if (verification.getVerifiedAt().plusMinutes(VERIFIED_VALIDITY_MINUTES).isBefore(LocalDateTime.now())) {
            throw CustomException.of(ErrorCode.PHONE_VERIFICATION_EXPIRED);
        }
    }

    @Transactional
    public void deleteVerification(String phone) {
        phoneVerificationRepository.deleteByPhone(phone);
    }
}
