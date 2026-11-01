package passroutebackend.selfintro.dto.response;

import lombok.Builder;
import lombok.Getter;
import passroutebackend.selfintro.entity.SelfIntro;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class SelfIntroResponseDto {

    private Long id;
    private String companyName;
    private String jobPosition;
    private String careerLevel;
    private String jobDescription;
    private String jobPostingUrl;
    private String memo;
    private LocalDate interviewDate;
    private String interviewTime;
    private String interviewStage;
    private int itemCount;
    private List<ItemDto> items;
    private int version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class ItemDto {
        private Long id;
        private String questionText;
        private String answerText;
        private int orderNum;
    }

    public static SelfIntroResponseDto from(SelfIntro entity) {
        return SelfIntroResponseDto.builder()
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