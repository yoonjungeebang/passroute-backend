package passroutebackend.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import passroutebackend.auth.dto.request.LoginRequest;
import passroutebackend.auth.dto.request.LogoutRequest;
import passroutebackend.auth.dto.request.PasswordResetRequest;
import passroutebackend.auth.dto.request.PhoneSendRequest;
import passroutebackend.auth.dto.request.PhoneVerifyRequest;
import passroutebackend.auth.dto.request.ReissueRequest;
import passroutebackend.auth.dto.request.SignUpRequest;
import passroutebackend.auth.dto.response.FindEmailResponse;
import passroutebackend.auth.dto.response.LoginResponse;
import passroutebackend.auth.service.AuthService;
import passroutebackend.auth.service.PhoneVerificationService;
import passroutebackend.global.ApiResponse;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PhoneVerificationService phoneVerificationService;

    @Operation(summary = "휴대폰 인증번호 발송", description = "회원가입 시 휴대폰 인증번호를 발송합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증번호 발송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(schema = @Schema(hidden = true))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입된 휴대폰번호", content = @Content(schema = @Schema(hidden = true)))
    })
    @SecurityRequirements
    @PostMapping("/phone/send")
    public ResponseEntity<ApiResponse<Void>> sendPhoneVerification(@Valid @RequestBody PhoneSendRequest request) {
        phoneVerificationService.sendVerificationCode(request.getPhone());
        return ResponseEntity.ok(ApiResponse.success("인증번호가 발송되었습니다.", null));
    }

    @Operation(summary = "휴대폰 인증번호 확인", description = "발송된 인증번호를 검증합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증번호 불일치 또는 만료", content = @Content(schema = @Schema(hidden = true)))
    })
    @SecurityRequirements
    @PostMapping("/phone/verify")
    public ResponseEntity<ApiResponse<Void>> verifyPhone(@Valid @RequestBody PhoneVerifyRequest request) {
        phoneVerificationService.verifyCode(request.getPhone(), request.getCode());
        return ResponseEntity.ok(ApiResponse.success("휴대폰 인증이 완료되었습니다.", null));
    }

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 휴대폰 인증을 통해 회원가입합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류 또는 휴대폰 미인증", content = @Content(schema = @Schema(hidden = true))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 이메일", content = @Content(schema = @Schema(hidden = true)))
    })
    @SecurityRequirements
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Long>> signUp(@Valid @RequestBody SignUpRequest request) {
        Long userId = authService.signUp(request);
        return ResponseEntity.ok(ApiResponse.success("회원가입 성공!", userId));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 JWT 토큰을 발급받습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치", content = @Content(schema = @Schema(hidden = true)))
    })
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success("로그인 성공!", response));
    }

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰으로 새로운 액세스 토큰과 리프레시 토큰을 발급받습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰", content = @Content(schema = @Schema(hidden = true)))
    })
    @SecurityRequirements
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<LoginResponse>> reissue(@Valid @RequestBody ReissueRequest request) {
        LoginResponse response = authService.reissue(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("토큰 재발급 성공!", response));
    }

    @Operation(summary = "아이디(이메일) 찾기 - 인증번호 발송", description = "가입된 휴대폰으로 인증번호를 발송합니다.")
    @SecurityRequirements
    @PostMapping("/find-email/send")
    public ResponseEntity<ApiResponse<Void>> sendFindEmailCode(@Valid @RequestBody PhoneSendRequest request) {
        phoneVerificationService.sendCodeForRegisteredPhone(request.getPhone());
        return ResponseEntity.ok(ApiResponse.success("인증번호가 발송되었습니다.", null));
    }

    @Operation(summary = "아이디(이메일) 찾기 - 인증 확인", description = "인증번호 확인 후 마스킹된 이메일을 반환합니다.")
    @SecurityRequirements
    @PostMapping("/find-email/confirm")
    public ResponseEntity<ApiResponse<FindEmailResponse>> confirmFindEmail(@Valid @RequestBody PhoneVerifyRequest request) {
        FindEmailResponse response = authService.findEmail(request.getPhone(), request.getCode());
        return ResponseEntity.ok(ApiResponse.success("이메일 조회 성공!", response));
    }

    @Operation(summary = "비밀번호 찾기 - 인증번호 발송", description = "가입된 휴대폰으로 비밀번호 재설정 인증번호를 발송합니다.")
    @SecurityRequirements
    @PostMapping("/find-password/send")
    public ResponseEntity<ApiResponse<Void>> sendPasswordResetCode(@Valid @RequestBody PhoneSendRequest request) {
        phoneVerificationService.sendCodeForRegisteredPhone(request.getPhone());
        return ResponseEntity.ok(ApiResponse.success("인증번호가 발송되었습니다.", null));
    }

    @Operation(summary = "비밀번호 재설정", description = "인증번호 확인 후 새 비밀번호로 변경합니다.")
    @SecurityRequirements
    @PostMapping("/find-password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request.getPhone(), request.getCode(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다.", null));
    }

    @Operation(summary = "로그아웃", description = "액세스 토큰과 리프레시 토큰을 모두 무효화합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String accessToken,
            @Valid @RequestBody LogoutRequest request) {

        authService.logout(accessToken, request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공!", null));
    }
}
