package passroutebackend.interview.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import passroutebackend.global.type.Difficulty;
import passroutebackend.interview.entity.InterviewFormat;
import passroutebackend.interview.entity.InterviewType;

@Schema(description = "면접 방 생성 요청")
@Getter
public class InterviewRoomRequest {

    @Schema(description = "자소서 ID")
    private Long siId;

    @Schema(description = "이력서 ID")
    private Long resumeId;

    @Schema(description = "포트폴리오 ID")
    private Long portfolioId;

    @Schema(description = "기업명", example = "네이버")
    @NotBlank
    private String companyName;

    @Schema(description = "직무", example = "백엔드 개발")
    @NotBlank
    private String jobPosition;

    @Schema(description = "면접 유형")
    @NotNull
    private InterviewType interviewType;

    @Schema(description = "면접 모드")
    @NotBlank
    private String interviewMode;

    @Schema(description = "면접 형식")
    @NotNull
    private InterviewFormat interviewFormat;

    @Schema(description = "AI 면접관")
    @NotBlank
    private String aiInterviewer;

    @Schema(description = "AI 경쟁자")
    private String aiCompetitors;

    @Schema(description = "토론 주제")
    private String debateTopic;

    @Schema(description = "질문 수 (1~10)", example = "5")
    @Min(1) @Max(10)
    private int interviewCount = 5;

    @Schema(description = "난이도")
    @NotNull
    private Difficulty difficulty;

    @Schema(description = "압박 수준 (0~10)", example = "5")
    @Min(0) @Max(10)
    private int pressureLevel = 5;

    @Schema(description = "꼬리질문 수 (0~5)", example = "3")
    @Min(0) @Max(5)
    private int followupCount = 3;
}
