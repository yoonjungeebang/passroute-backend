package passroutebackend.interview.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import passroutebackend.global.service.PersonaVideoService;
import passroutebackend.interview.client.AiServerClient;
import passroutebackend.interview.dto.generate.QuestionGenerateRequest;
import passroutebackend.interview.dto.generate.QuestionGenerateResponse;
import passroutebackend.interview.dto.generate.SessionPreparation;
import passroutebackend.interview.dto.request.InterviewStartRequest;
import passroutebackend.interview.dto.response.InterviewStartResponse;
import passroutebackend.interview.dto.response.PersonaVideoResponse;
import passroutebackend.interview.dto.response.QuestionDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewStartServiceTest {

  @Mock private InterviewStartTxService txService;
  @Mock private AiServerClient aiServerClient;
  @Mock private PersonaVideoService personaVideoService;
  @InjectMocks private InterviewStartService interviewStartService;

  @Test
  void includesInterviewerVideoUrlsInStartResponse() {
    InterviewStartRequest request = new InterviewStartRequest();
    ReflectionTestUtils.setField(request, "roomId", 10L);
    QuestionGenerateRequest aiRequest = org.mockito.Mockito.mock(QuestionGenerateRequest.class);
    QuestionGenerateResponse aiResponse = org.mockito.Mockito.mock(QuestionGenerateResponse.class);
    List<QuestionGenerateResponse.QuestionItem> generatedQuestions = List.of();
    List<QuestionDto> savedQuestions = List.of(new QuestionDto(1L, "질문", 1, null));

    when(txService.prepareSession(10L, 1L))
        .thenReturn(new SessionPreparation(20L, aiRequest, "TECH_INTERVIEWER"));
    when(aiServerClient.generateQuestions(aiRequest)).thenReturn(aiResponse);
    when(aiResponse.getQuestions()).thenReturn(generatedQuestions);
    when(txService.saveQuestions(20L, generatedQuestions)).thenReturn(savedQuestions);
    when(personaVideoService.getInterviewer("TECH_INTERVIEWER"))
        .thenReturn(new PersonaVideoResponse(
            "https://s3/interviewer-speaking.mp4",
            "https://s3/interviewer-silence.mp4"));

    InterviewStartResponse response = interviewStartService.start(1L, request);

    assertThat(response.getSessionId()).isEqualTo(20L);
    assertThat(response.getQuestions()).isEqualTo(savedQuestions);
    assertThat(response.getInterviewer().getSpeakingVideoUrl())
        .isEqualTo("https://s3/interviewer-speaking.mp4");
    assertThat(response.getInterviewer().getSilenceVideoUrl())
        .isEqualTo("https://s3/interviewer-silence.mp4");
  }
}
