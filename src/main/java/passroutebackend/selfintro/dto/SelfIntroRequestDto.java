package passroutebackend.selfintro.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import passroutebackend.selfintro.entity.SelfIntro;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class SelfIntroRequestDto {

    @NotBlank(message = "기업명은 필수입니다.")
    private String companyName;

    @NotBlank(message = "직무는 필수입니다.")
    private String jobPosition;

    @NotNull(message = "경력 구분은 필수입니다.")
    private SelfIntro.CareerLevel careerLevel;

    private String jobDescription;
    private String jobPostingUrl;
    private String memo;

    private LocalDate interviewDate;
    private String interviewTime;
    private SelfIntro.InterviewStage interviewStage;

    @Valid
    private List<ItemDto> items = new ArrayList<>();

    @Getter
    @NoArgsConstructor
    public static class ItemDto {

        @NotBlank(message = "질문을 입력해주세요.")
        private String questionText;

        private String answerText;
    }
}