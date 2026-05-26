package passroutebackend.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import passroutebackend.interview.client.AiServerClient;
import passroutebackend.interview.dto.generate.QuestionGenerateResponse;
import passroutebackend.interview.dto.generate.SessionPreparation;
import passroutebackend.interview.dto.request.InterviewStartRequest;
import passroutebackend.interview.dto.response.InterviewStartResponse;
import passroutebackend.interview.dto.response.QuestionDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewStartService {

  private final InterviewStartTxService txService;
  private final AiServerClient aiServerClient;

  public InterviewStartResponse start(Long userId, InterviewStartRequest request) {
    SessionPreparation prep = txService.prepareSession(request.getRoomId(), userId);

    QuestionGenerateResponse aiResponse = aiServerClient.generateQuestions(prep.getAiRequest());

    List<QuestionDto> questions = txService.saveQuestions(prep.getSessionId(), aiResponse.getQuestionTexts());

    return new InterviewStartResponse(prep.getSessionId(), questions);
  }
}
