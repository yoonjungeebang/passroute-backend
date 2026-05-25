package passroutebackend.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.document.entity.Document;
import passroutebackend.document.entity.Document.DocumentType;
import passroutebackend.document.entity.DocumentAnalysis;
import passroutebackend.document.repository.DocumentAnalysisRepository;
import passroutebackend.document.repository.DocumentRepository;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InterviewStartTxService {

  private final InterviewRoomRepository interviewRoomRepository;
  private final InterviewSessionRepository interviewSessionRepository;
  private final InterviewQuestionRepository interviewQuestionRepository;
  private final SelfIntroRepository selfIntroRepository;
  private final DocumentRepository documentRepository;
  private final DocumentAnalysisRepository documentAnalysisRepository;

  @Transactional
  public SessionPreparation prepareSession(Long roomId, Long userId) {
    InterviewRoom room = interviewRoomRepository.findById(roomId)
        .orElseThrow(() -> CustomException.of(ErrorCode.ROOM_NOT_FOUND));

    if (!room.getUserId().equals(userId)) {
      throw CustomException.of(ErrorCode.ACCESS_DENIED);
    }

    List<SelfIntroItemDto> selfIntroItems = loadSelfIntroItems(room.getSiId());

    String resumeText = extractedText(userId, DocumentType.RESUME);
    String portfolioText = extractedText(userId, DocumentType.PORTFOLIO);

    int sessionNumber = interviewSessionRepository.countByInterviewRoom(room) + 1;
    InterviewSession session = InterviewSession.builder()
        .interviewRoom(room)
        .sessionNumber(sessionNumber)
        .status(SessionStatus.IN_PROGRESS)
        .build();
    interviewSessionRepository.save(session);

    room.updateStatus(RoomStatus.IN_PROGRESS);

    String coverLetter = selfIntroItems.stream()
        .map(item -> item.getQuestion() + "\n" + item.getAnswer())
        .reduce((a, b) -> a + "\n\n" + b)
        .orElse("");

    QuestionGenerateRequest aiRequest = new QuestionGenerateRequest(
        room.getInterviewType().name(),
        room.getDifficulty().name(),
        room.getCompanyName(),
        room.getJobPosition(),
        room.getInterviewCount(),
        resumeText,
        portfolioText,
        room.getAiInterviewer(),
        room.getPressureLevel(),
        room.getFollowupCount(),
        room.getInterviewFormat().name(),
        coverLetter
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

  private String extractedText(Long userId, DocumentType type) {
    return documentRepository
        .findByUserIdAndTypeAndIsRepresentativeTrueAndDeletedAtIsNull(userId, type)
        .flatMap(documentAnalysisRepository::findByDocument)
        .map(DocumentAnalysis::getExtractedText)
        .orElse(null);
  }
}
