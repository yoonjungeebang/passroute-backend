package passroutebackend.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.dto.response.HistoryDetailResponse;
import passroutebackend.interview.dto.response.HistoryDetailResponse.QuestionAnswer;
import passroutebackend.interview.dto.response.HistoryDetailResponse.SessionDetail;
import passroutebackend.interview.dto.response.HistoryListResponse;
import passroutebackend.interview.dto.response.InterviewRoomResponseDto;
import passroutebackend.interview.entity.InterviewAnswer;
import passroutebackend.interview.entity.InterviewFormat;
import passroutebackend.interview.entity.InterviewQuestion;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.entity.InterviewType;
import passroutebackend.interview.entity.RoomStatus;
import passroutebackend.interview.repository.InterviewAnswerRepository;
import passroutebackend.interview.repository.InterviewQuestionRepository;
import passroutebackend.interview.repository.InterviewRoomRepository;
import passroutebackend.interview.repository.InterviewSessionRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewHistoryService {

    private final InterviewRoomRepository interviewRoomRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewAnswerRepository interviewAnswerRepository;

    @Transactional(readOnly = true)
    public Page<HistoryListResponse> getList(Long userId, InterviewType type,
                                              InterviewFormat format, Pageable pageable) {
        return interviewRoomRepository
                .findHistories(userId, RoomStatus.COMPLETED, type, format, pageable)
                .map(HistoryListResponse::from);
    }

    @Transactional(readOnly = true)
    public HistoryDetailResponse getDetail(Long userId, Long roomId) {
        InterviewRoom room = findRoomByIdAndValidateOwner(userId, roomId);

        if (room.getStatus() != RoomStatus.COMPLETED) {
            throw CustomException.of(ErrorCode.ROOM_NOT_COMPLETED);
        }

        List<InterviewSession> sessions = interviewSessionRepository
                .findByInterviewRoomAndDeletedAtIsNullOrderBySessionNumberAsc(room);

        List<InterviewQuestion> allQuestions = interviewQuestionRepository
                .findBySessionInOrderBySessionIdAscQuestionOrderAsc(sessions);

        List<InterviewAnswer> allAnswers = interviewAnswerRepository
                .findByQuestionIn(allQuestions);

        Map<Long, List<InterviewQuestion>> questionsBySessionId = allQuestions.stream()
                .collect(Collectors.groupingBy(q -> q.getSession().getId()));

        Map<Long, InterviewAnswer> answerByQuestionId = allAnswers.stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), Function.identity()));

        List<SessionDetail> sessionDetails = sessions.stream()
                .map(session -> toSessionDetail(session, questionsBySessionId, answerByQuestionId))
                .toList();

        return HistoryDetailResponse.of(room, sessionDetails);
    }

    @Transactional
    public InterviewRoomResponseDto retry(Long userId, Long roomId) {
        InterviewRoom originalRoom = findRoomByIdAndValidateOwner(userId, roomId);

        InterviewRoom newRoom = InterviewRoom.builder()
                .userId(userId)
                .companyName(originalRoom.getCompanyName())
                .jobPosition(originalRoom.getJobPosition())
                .interviewType(originalRoom.getInterviewType())
                .interviewFormat(originalRoom.getInterviewFormat())
                .interviewMode(originalRoom.getInterviewMode())
                .aiInterviewer(originalRoom.getAiInterviewer())
                .aiCompetitors(originalRoom.getAiCompetitors())
                .debateTopic(originalRoom.getDebateTopic())
                .interviewCount(originalRoom.getInterviewCount())
                .difficulty(originalRoom.getDifficulty())
                .pressureLevel(originalRoom.getPressureLevel())
                .followupCount(originalRoom.getFollowupCount())
                .status(RoomStatus.DRAFT)
                .build();

        interviewRoomRepository.save(newRoom);
        return new InterviewRoomResponseDto(newRoom.getId());
    }

    @Transactional(readOnly = true)
    public Page<HistoryListResponse> search(Long userId, String keyword, Pageable pageable) {
        return interviewRoomRepository
                .searchByKeyword(userId, RoomStatus.COMPLETED, keyword, pageable)
                .map(HistoryListResponse::from);
    }

    private InterviewRoom findRoomByIdAndValidateOwner(Long userId, Long roomId) {
        InterviewRoom room = interviewRoomRepository.findById(roomId)
                .orElseThrow(() -> CustomException.of(ErrorCode.ROOM_NOT_FOUND));

        if (!room.getUserId().equals(userId)) {
            throw CustomException.of(ErrorCode.ROOM_ACCESS_DENIED);
        }

        return room;
    }

    private SessionDetail toSessionDetail(InterviewSession session,
                                         Map<Long, List<InterviewQuestion>> questionsBySessionId,
                                         Map<Long, InterviewAnswer> answerByQuestionId) {
        List<InterviewQuestion> questions = questionsBySessionId
                .getOrDefault(session.getId(), List.of())
                .stream()
                .sorted(Comparator.comparingInt(InterviewQuestion::getSetNumber)
                        .thenComparingInt(InterviewQuestion::getQuestionOrder))
                .toList();

        List<QuestionAnswer> questionAnswers = questions.stream()
                .map(q -> {
                    InterviewAnswer answer = answerByQuestionId.get(q.getId());
                    return new QuestionAnswer(
                            q.getId(),
                            q.getQuestionText(),
                            q.getQuestionOrder(),
                            q.isFollowUp(),
                            answer != null ? answer.getAnswerText() : null,
                            answer != null ? answer.getPercentage() : null,
                            answer != null ? answer.getStarScore() : null
                    );
                })
                .toList();

        return new SessionDetail(
                session.getId(),
                session.getSessionNumber(),
                session.getStatus().name(),
                session.getStartedAt(),
                session.getEndedAt(),
                questionAnswers
        );
    }
}
