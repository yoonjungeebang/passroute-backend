package passroutebackend.selfintro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import passroutebackend.global.ApiResponse;
import passroutebackend.selfintro.dto.request.SelfIntroRequest;
import passroutebackend.selfintro.dto.response.SelfIntroResponse;
import passroutebackend.selfintro.service.SelfIntroService;

import java.util.List;

@Tag(name = "SelfIntro", description = "자소서 API")
@RestController
@RequestMapping("/self-intro")
@RequiredArgsConstructor
public class SelfIntroController {

    private final SelfIntroService selfIntroService;

    @Operation(summary = "자소서 목록 조회", description = "필터(all/upcoming/past)를 적용하여 자소서 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<SelfIntroResponse>>> getList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "all") String filter) {
        return ResponseEntity.ok(ApiResponse.success(selfIntroService.getList(userId, filter)));
    }

    @Operation(summary = "자소서 단건 조회", description = "자소서 ID로 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "자소서를 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SelfIntroResponse>> getOne(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(selfIntroService.getOne(userId, id)));
    }

    @Operation(summary = "자소서 생성", description = "새 자소서를 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "자소서 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<SelfIntroResponse>> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid SelfIntroRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(selfIntroService.create(userId, dto)));
    }

    @Operation(summary = "자소서 수정", description = "기존 자소서를 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "자소서를 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SelfIntroResponse>> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody @Valid SelfIntroRequest dto) {
        return ResponseEntity.ok(ApiResponse.success(selfIntroService.update(userId, id, dto)));
    }

    @Operation(summary = "자소서 삭제", description = "자소서를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "자소서를 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        selfIntroService.delete(userId, id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
