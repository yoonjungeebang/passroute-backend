package passroutebackend.interview.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.dto.ai.request.QuestionGenerateRequest;
import passroutebackend.interview.dto.ai.common.SessionPreparation;
import passroutebackend.interview.dto.request.InterviewStartRequest;
import passroutebackend.interview.dto.response.InterviewStartResponse;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.entity.SessionStatus;

import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewStartServiceTest {

  @Mock private InterviewStartTxService txService;
  @Mock private InterviewStartAsyncService asyncService;
  @InjectMocks private InterviewStartService service;

  @Test
  void returnsExistingSessionWhenAlreadyActive() {
    InterviewStartRequest request = new InterviewStartRequest();
    ReflectionTestUtils.setField(request, "roomId", 10L);

    InterviewSession existing = mock(InterviewSession.class);
    when(existing.getId()).thenReturn(20L);
    when(existing.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(txService.findActiveSession(10L)).thenReturn(Optional.of(existing));

    InterviewStartResponse response = service.start(1L, request);

    assertThat(response.getSessionId()).isEqualTo(20L);
    assertThat(response.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
    verify(asyncService, never()).prepareInBackground(any());
  }

  @Test
  void createsPreparingSessionAndDelegatesAsync() {
    InterviewStartRequest request = new InterviewStartRequest();
    ReflectionTestUtils.setField(request, "roomId", 10L);

    when(txService.findActiveSession(10L)).thenReturn(Optional.empty());
    QuestionGenerateRequest aiRequest = mock(QuestionGenerateRequest.class);
    SessionPreparation prep = new SessionPreparation(20L, aiRequest, "TECH_INTERVIEWER");
    when(txService.createPreparingSession(10L, 1L)).thenReturn(prep);

    InterviewStartResponse response = service.start(1L, request);

    assertThat(response.getSessionId()).isEqualTo(20L);
    assertThat(response.getStatus()).isEqualTo(SessionStatus.PREPARING);
    verify(asyncService).prepareInBackground(eq(prep));
  }

  @Test
  void throwsBusyWhenThreadPoolExhausted() {
    InterviewStartRequest request = new InterviewStartRequest();
    ReflectionTestUtils.setField(request, "roomId", 10L);

    when(txService.findActiveSession(10L)).thenReturn(Optional.empty());
    QuestionGenerateRequest aiRequest = mock(QuestionGenerateRequest.class);
    SessionPreparation prep = new SessionPreparation(20L, aiRequest, "TECH_INTERVIEWER");
    when(txService.createPreparingSession(10L, 1L)).thenReturn(prep);
    doThrow(new RejectedExecutionException()).when(asyncService).prepareInBackground(prep);

    assertThatThrownBy(() -> service.start(1L, request))
        .isInstanceOf(CustomException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.INTERVIEW_START_BUSY));
    verify(txService).markPrepareFailed(20L);
  }
}