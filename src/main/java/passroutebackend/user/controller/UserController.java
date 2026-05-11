package passroutebackend.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import passroutebackend.global.ApiResponse;
import passroutebackend.user.dto.request.WithdrawRequest;
import passroutebackend.user.service.UserService;

@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원탈퇴", description = "계정을 탈퇴합니다. 소셜 로그인 계정은 password 없이 요청하세요.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "비밀번호 불일치")
    })
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal Long userId,
            @RequestHeader("Authorization") String accessToken,
            @RequestBody(required = false) WithdrawRequest request) {
        userService.withdraw(userId, accessToken, request != null ? request.password() : null);
        return ResponseEntity.ok(ApiResponse.success("회원탈퇴가 완료되었습니다.", null));
    }
}
