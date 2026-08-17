package passroutebackend.selfintro.controller;

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
import passroutebackend.selfintro.dto.SelfIntroRequestDto;
import passroutebackend.selfintro.dto.SelfIntroResponseDto;
import passroutebackend.selfintro.service.SelfIntroService;

import java.util.List;

@Tag(name = "SelfIntro", description = "자소서 API")
@RestController
@RequestMapping("/self-intro")
@RequiredArgsConstructor
public class SelfIntroController {

    private final SelfIntroService selfIntroService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SelfIntroResponseDto>>> getList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "all") String filter) {
        return ResponseEntity.ok(ApiResponse.success(selfIntroService.getList(userId, filter)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SelfIntroResponseDto>> getOne(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(selfIntroService.getOne(userId, id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SelfIntroResponseDto>> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid SelfIntroRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(selfIntroService.create(userId, dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SelfIntroResponseDto>> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody @Valid SelfIntroRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(selfIntroService.update(userId, id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        selfIntroService.delete(userId, id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
