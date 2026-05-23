package passroutebackend.interview.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.interview.entity.Difficulty;
import passroutebackend.interview.entity.InterviewFormat;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewType;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class HistoryListResponse {

    private Long roomId;
    private String companyName;
    private String jobPosition;
    private InterviewType interviewType;
    private InterviewFormat interviewFormat;
    private Difficulty difficulty;
    private int interviewCount;
    private LocalDateTime createdAt;
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
