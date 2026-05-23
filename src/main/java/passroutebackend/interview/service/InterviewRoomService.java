package passroutebackend.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.dto.request.InterviewRoomRequestDto;
import passroutebackend.interview.dto.response.InterviewRoomResponseDto;
import passroutebackend.interview.entity.InterviewFormat;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.RoomStatus;
import passroutebackend.interview.repository.InterviewRoomRepository;

@Service
@RequiredArgsConstructor
public class InterviewRoomService {

    private final InterviewRoomRepository interviewRoomRepository;

    @Transactional
    public InterviewRoomResponseDto setup(Long userId, InterviewRoomRequestDto request) {
        validateFormatConstraints(request);

        InterviewRoom room = InterviewRoom.builder()
                .userId(userId)
                .siId(request.getSiId())
                .companyName(request.getCompanyName())
                .jobPosition(request.getJobPosition())
                .interviewType(request.getInterviewType())
                .interviewFormat(request.getInterviewFormat())
                .interviewMode(request.getInterviewMode())
                .aiInterviewer(request.getAiInterviewer())
                .aiCompetitors(request.getAiCompetitors())
                .debateTopic(request.getDebateTopic())
                .interviewCount(request.getInterviewCount())
                .difficulty(request.getDifficulty())
                .pressureLevel(request.getPressureLevel())
                .followupCount(request.getFollowupCount())
                .status(RoomStatus.DRAFT)
                .build();

        interviewRoomRepository.save(room);
        return new InterviewRoomResponseDto(room.getId());
    }

    private void validateFormatConstraints(InterviewRoomRequestDto request) {
        if (InterviewFormat.DEBATE == request.getInterviewFormat()) {
            if (request.getAiCompetitors() == null || request.getDebateTopic() == null) {
                throw CustomException.of(ErrorCode.INVALID_INTERVIEW_FORMAT);
            }
        } else {
            if (request.getAiCompetitors() != null || request.getDebateTopic() != null) {
                throw CustomException.of(ErrorCode.INVALID_INTERVIEW_FORMAT);
            }
        }
    }
}
