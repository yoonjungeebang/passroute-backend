package passroutebackend.interview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.global.type.Difficulty;
import passroutebackend.interview.entity.InterviewFormat;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewType;

import java.time.LocalDateTime;

@Schema(description = "면접 이력 목록 항목")
@Getter
@AllArgsConstructor
public class HistoryListResponse {

    @Schema(description = "면접 방 ID")
    private Long roomId;
    @Schema(description = "기업명")
    private String companyName;
    @Schema(description = "직무")
    private String jobPosition;
    @Schema(description = "면접 유형")
    private InterviewType interviewType;
    @Schema(description = "면접 형식")
    private InterviewFormat interviewFormat;
    @Schema(description = "난이도")
    private Difficulty difficulty;
    @Schema(description = "질문 수")
    private int interviewCount;
    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;
    @Schema(description = "완료 일시")
    private LocalDateTime completedAt;

    public static HistoryListResponse from(InterviewRoom room) {
        return new HistoryListResponse(
                room.getId(),
                room.getCompanyName(),
                room.getJobPosition(),
                room.getInterviewType(),
                room.getInterviewFormat(),
                room.getDifficulty(),
                room.getInterviewCount(),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }
}
