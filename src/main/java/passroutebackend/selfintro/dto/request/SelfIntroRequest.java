package passroutebackend.selfintro.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import passroutebackend.selfintro.entity.SelfIntro;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "자소서 생성/수정 요청")
@Getter
@NoArgsConstructor
public class SelfIntroRequest {

    @Schema(description = "기업명", example = "네이버")
    @NotBlank(message = "기업명은 필수입니다.")
    private String companyName;

    @Schema(description = "직무", example = "백엔드 개발")
    @NotBlank(message = "직무는 필수입니다.")
    private String jobPosition;

    @Schema(description = "경력 구분", example = "NEW")
    @NotNull(message = "경력 구분은 필수입니다.")
    private SelfIntro.CareerLevel careerLevel;

    @Schema(description = "직무 기술서")
    private String jobDescription;
    @Schema(description = "채용 공고 URL")
    private String jobPostingUrl;
    @Schema(description = "메모")
    private String memo;

    @Schema(description = "면접 날짜", example = "2026-12-01")
    private LocalDate interviewDate;
    @Schema(description = "면접 시간", example = "14:00")
    private String interviewTime;
    @Schema(description = "면접 단계")
    private SelfIntro.InterviewStage interviewStage;

    @Schema(description = "자소서 항목 목록")
    @Valid
    private List<ItemDto> items = new ArrayList<>();

    @Schema(description = "자소서 항목")
    @Getter
    @NoArgsConstructor
    public static class ItemDto {

        @Schema(description = "질문", example = "지원동기를 작성해주세요.")
        @NotBlank(message = "질문을 입력해주세요.")
        private String questionText;

        @Schema(description = "답변")
        private String answerText;
    }
}