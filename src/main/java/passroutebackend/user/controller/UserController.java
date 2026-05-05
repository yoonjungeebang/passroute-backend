package passroutebackend.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import passroutebackend.global.ApiResponse;
import passroutebackend.user.dto.*;
import passroutebackend.user.service.PhoneVerificationService;
import passroutebackend.user.service.UserService;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
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
        Long userId = userService.signUp(request);
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
        LoginResponse response = userService.login(request.getEmail(), request.getPassword());
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
        LoginResponse response = userService.reissue(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("토큰 재발급 성공!", response));
    }
}
