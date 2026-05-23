package passroutebackend.interview.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.interview.entity.Difficulty;
import passroutebackend.interview.entity.InterviewFormat;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class HistoryDetailResponse {

    private Long roomId;
    private String companyName;
    private String jobPosition;
    private InterviewType interviewType;
    private InterviewFormat interviewFormat;
    private Difficulty difficulty;
    private int interviewCount;
    private int pressureLevel;
    private int followupCount;
    private String interviewMode;
    private String aiInterviewer;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private List<SessionDetail> sessions;

    public static HistoryDetailResponse of(InterviewRoom room, List<SessionDetail> sessions) {
        return new HistoryDetailResponse(
                room.getId(),
                room.getCompanyName(),
                room.getJobPosition(),
                room.getInterviewType(),
                room.getInterviewFormat(),
                room.getDifficulty(),
                room.getInterviewCount(),
                room.getPressureLevel(),
                room.getFollowupCount(),
                room.getInterviewMode(),
                room.getAiInterviewer(),
                room.getCreatedAt(),
                room.getUpdatedAt(),
                sessions
        );
    }

    @Getter
    @AllArgsConstructor
    public static class SessionDetail {
        private Long sessionId;
        private int sessionNumber;
        private String status;
        private LocalDateTime startedAt;
        private LocalDateTime endedAt;
        private List<QuestionAnswer> questionAnswers;
    }

    @Getter
    @AllArgsConstructor
    public static class QuestionAnswer {
        private Long questionId;
        private String questionText;
        private int questionOrder;
        private boolean followUp;
        private String answerText;
        private Double percentage;
        private Integer starScore;
    }
}
