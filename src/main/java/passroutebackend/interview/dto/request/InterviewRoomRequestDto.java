package passroutebackend.interview.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import passroutebackend.interview.entity.Difficulty;
import passroutebackend.interview.entity.InterviewFormat;
import passroutebackend.interview.entity.InterviewType;

@Getter
public class InterviewRoomRequestDto {

    private Long siId;

    @NotBlank
    private String companyName;

    @NotBlank
    private String jobPosition;

    @NotNull
    private InterviewType interviewType;

    @NotBlank
    private String interviewMode;

    @NotNull
    private InterviewFormat interviewFormat;

    @NotBlank
    private String aiInterviewer;

    private String aiCompetitors;

    private String debateTopic;

    @Min(1) @Max(20)
    private int interviewCount = 5;

    @NotNull
    private Difficulty difficulty;

    @Min(0) @Max(10)
    private int pressureLevel = 5;

    @Min(0) @Max(5)
    private int followupCount = 3;
}
