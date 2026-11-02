package passroutebackend.interview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.global.type.Difficulty;
import passroutebackend.interview.entity.InterviewFormat;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewType;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "면접 이력 상세 응답")
@Getter
@AllArgsConstructor
public class HistoryDetailResponse {

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
    @Schema(description = "압박 수준")
    private int pressureLevel;
    @Schema(description = "꼬리질문 수")
    private int followupCount;
    @Schema(description = "면접 모드")
    private String interviewMode;
    @Schema(description = "AI 면접관")
    private String aiInterviewer;
    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;
    @Schema(description = "완료 일시")
    private LocalDateTime completedAt;
    @Schema(description = "세션 목록")
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

    @Schema(description = "세션 상세")
    @Getter
    @AllArgsConstructor
    public static class SessionDetail {
        @Schema(description = "세션 ID")
        private Long sessionId;
        @Schema(description = "세션 번호")
        private int sessionNumber;
        @Schema(description = "세션 상태")
        private String status;
        @Schema(description = "시작 일시")
        private LocalDateTime startedAt;
        @Schema(description = "종료 일시")
        private LocalDateTime endedAt;
        @Schema(description = "질문-답변 목록")
        private List<QuestionAnswer> questionAnswers;
    }

    @Schema(description = "질문-답변")
    @Getter
    @AllArgsConstructor
    public static class QuestionAnswer {
        @Schema(description = "질문 ID")
        private Long questionId;
        @Schema(description = "질문 텍스트")
        private String questionText;
        @Schema(description = "질문 순서")
        private int questionOrder;
        @Schema(description = "꼬리질문 여부")
        private boolean followUp;
        @Schema(description = "답변 텍스트")
        private String answerText;
        @Schema(description = "점수 (%)")
        private Double percentage;
        @Schema(description = "별점")
        private Integer starScore;
    }
}
