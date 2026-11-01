package passroutebackend.interview.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.global.property.InterviewPreparationProperties;
import passroutebackend.interview.dto.generate.QuestionGenerateRequest;
import passroutebackend.interview.dto.generate.SessionPreparation;
import passroutebackend.interview.dto.request.InterviewStartRequest;
import passroutebackend.interview.dto.response.InterviewStartResponse;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.entity.SessionStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewStartServiceTest {

  @Mock private InterviewStartTxService txService;
  @Mock private InterviewStartAsyncService asyncService;

  private InterviewStartService createService(int maxConcurrent) {
    InterviewPreparationProperties props = new InterviewPreparationProperties();
    props.setMaxConcurrent(maxConcurrent);
    return new InterviewStartService(txService, asyncService, props);
  }

  @Test
  void returnsExistingSessionWhenAlreadyActive() {
    InterviewStartService service = createService(3);
    InterviewStartRequest request = new InterviewStartRequest();
    ReflectionTestUtils.setField(request, "roomId", 10L);

    InterviewSession existing = mock(InterviewSession.class);
    when(existing.getId()).thenReturn(20L);
    when(existing.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(txService.findActiveSession(10L)).thenReturn(Optional.of(existing));

    InterviewStartResponse response = service.start(1L, request);

    assertThat(response.getSessionId()).isEqualTo(20L);
    assertThat(response.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
    verify(asyncService, never()).prepareInBackground(any(), any());
  }

  @Test
  void createsPreparingSessionAndDelegatesAsync() {
    InterviewStartService service = createService(3);
    InterviewStartRequest request = new InterviewStartRequest();
    ReflectionTestUtils.setField(request, "roomId", 10L);

    when(txService.findActiveSession(10L)).thenReturn(Optional.empty());
    QuestionGenerateRequest aiRequest = mock(QuestionGenerateRequest.class);
    SessionPreparation prep = new SessionPreparation(20L, aiRequest, "TECH_INTERVIEWER");
    when(txService.createPreparingSession(10L, 1L)).thenReturn(prep);

    InterviewStartResponse response = service.start(1L, request);

    assertThat(response.getSessionId()).isEqualTo(20L);
    assertThat(response.getStatus()).isEqualTo(SessionStatus.PREPARING);
    verify(asyncService).prepareInBackground(eq(prep), any());
  }

  @Test
  void throwsBusyWhenSemaphoreExhausted() {
    InterviewStartService service = createService(0);
    InterviewStartRequest request = new InterviewStartRequest();
    ReflectionTestUtils.setField(request, "roomId", 10L);

    when(txService.findActiveSession(10L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.start(1L, request))
        .isInstanceOf(CustomException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.INTERVIEW_START_BUSY));
  }
}