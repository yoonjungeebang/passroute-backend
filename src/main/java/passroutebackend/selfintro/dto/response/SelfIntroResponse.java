package passroutebackend.selfintro.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.selfintro.entity.SelfIntro;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "자소서 응답")
@Getter
@Builder
public class SelfIntroResponse {

    @Schema(description = "자소서 ID")
    private Long id;
    @Schema(description = "기업명")
    private String companyName;
    @Schema(description = "직무")
    private String jobPosition;
    @Schema(description = "경력 구분")
    private String careerLevel;
    @Schema(description = "직무 기술서")
    private String jobDescription;
    @Schema(description = "채용 공고 URL")
    private String jobPostingUrl;
    @Schema(description = "메모")
    private String memo;
    @Schema(description = "면접 날짜")
    private LocalDate interviewDate;
    @Schema(description = "면접 시간")
    private String interviewTime;
    @Schema(description = "면접 단계")
    private String interviewStage;
    @Schema(description = "항목 수")
    private int itemCount;
    @Schema(description = "자소서 항목 목록")
    private List<ItemDto> items;
    @Schema(description = "버전")
    private int version;
    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;
    @Schema(description = "수정 일시")
    private LocalDateTime updatedAt;

    @Schema(description = "자소서 항목")
    @Getter
    @Builder
    public static class ItemDto {
        @Schema(description = "항목 ID")
        private Long id;
        @Schema(description = "질문")
        private String questionText;
        @Schema(description = "답변")
        private String answerText;
        @Schema(description = "정렬 순서")
        private int orderNum;
    }

    public static SelfIntroResponse from(SelfIntro entity) {
        return SelfIntroResponse.builder()
                .id(entity.getId())
                .companyName(entity.getCompanyName())
                .jobPosition(entity.getJobPosition())
                .careerLevel(entity.getCareerLevel().name())
                .jobDescription(entity.getJobDescription())
                .jobPostingUrl(entity.getJobPostingUrl())
                .memo(entity.getMemo())
                .interviewDate(entity.getInterviewDate())
                .interviewTime(entity.getInterviewTime())
                .interviewStage(entity.getInterviewStage() != null ? entity.getInterviewStage().name() : null)
                .itemCount(entity.getItems().size())
                .items(entity.getItems().stream()
                        .map(item -> ItemDto.builder()
                                .id(item.getId())
                                .questionText(item.getQuestionText())
                                .answerText(item.getAnswerText())
                                .orderNum(item.getOrderNum())
                                .build())
                        .collect(Collectors.toList()))
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}