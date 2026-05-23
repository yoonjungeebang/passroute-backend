package passroutebackend.interview.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class InterviewStartResponse {

  private Long sessionId;
  private List<QuestionDto> questions;
}
