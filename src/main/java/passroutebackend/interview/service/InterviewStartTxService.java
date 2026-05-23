package passroutebackend.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.dto.generate.QuestionGenerateRequest;
import passroutebackend.interview.dto.generate.SelfIntroItemDto;
import passroutebackend.interview.dto.generate.SessionPreparation;
import passroutebackend.interview.dto.response.QuestionDto;
import passroutebackend.interview.entity.InterviewQuestion;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.entity.RoomStatus;
import passroutebackend.interview.entity.SessionStatus;
import passroutebackend.interview.repository.InterviewQuestionRepository;
import passroutebackend.interview.repository.InterviewRoomRepository;
import passroutebackend.interview.repository.InterviewSessionRepository;
import passroutebackend.selfintro.repository.SelfIntroRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewStartTxService {

  private final InterviewRoomRepository interviewRoomRepository;
  private final InterviewSessionRepository interviewSessionRepository;
  private final InterviewQuestionRepository interviewQuestionRepository;
  private final SelfIntroRepository selfIntroRepository;

  @Transactional
  public SessionPreparation prepareSession(Long roomId, Long userId) {
    InterviewRoom room = interviewRoomRepository.findById(roomId)
        .orElseThrow(() -> CustomException.of(ErrorCode.ROOM_NOT_FOUND));

    if (!room.getUserId().equals(userId)) {
      throw CustomException.of(ErrorCode.ACCESS_DENIED);
    }

    List<SelfIntroItemDto> selfIntroItems = loadSelfIntroItems(room.getSiId());

    int sessionNumber = interviewSessionRepository.countByInterviewRoom(room) + 1;
    InterviewSession session = InterviewSession.builder()
        .interviewRoom(room)
        .sessionNumber(sessionNumber)
        .status(SessionStatus.IN_PROGRESS)
        .build();
    interviewSessionRepository.save(session);

    room.updateStatus(RoomStatus.IN_PROGRESS);

    QuestionGenerateRequest aiRequest = new QuestionGenerateRequest(
        room.getInterviewType().getValue(),
        room.getDifficulty().getValue(),
        room.getCompanyName(),
        room.getJobPosition(),
        room.getInterviewCount(),
        selfIntroItems
    );

    return new SessionPreparation(session.getId(), aiRequest);
  }

  @Transactional
  public List<QuestionDto> saveQuestions(Long sessionId, List<String> questionTexts) {
    InterviewSession session = interviewSessionRepository.getReferenceById(sessionId);

    List<InterviewQuestion> questions = new ArrayList<>();
    for (int i = 0; i < questionTexts.size(); i++) {
      questions.add(InterviewQuestion.builder()
          .session(session)
          .setNumber(1)
          .questionText(questionTexts.get(i))
          .questionOrder(i + 1)
          .followUp(false)
          .build());
    }
    interviewQuestionRepository.saveAll(questions);

    return questions.stream()
        .map(q -> new QuestionDto(q.getId(), q.getQuestionText(), q.getQuestionOrder()))
        .toList();
  }

  private List<SelfIntroItemDto> loadSelfIntroItems(Long siId) {
    if (siId == null) {
      return List.of();
    }
    return selfIntroRepository.findById(siId)
        .map(si -> si.getItems().stream()
            .map(item -> new SelfIntroItemDto(item.getQuestionText(), item.getAnswerText()))
            .toList())
        .orElse(List.of());
  }
}
