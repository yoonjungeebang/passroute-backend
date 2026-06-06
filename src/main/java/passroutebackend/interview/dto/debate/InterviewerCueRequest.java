package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * POST /debate/interviewer-cue 요청. cue_type만 보낸다(정형 템플릿이라 컨텍스트 불필요).
 */
@Getter
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class InterviewerCueRequest {

  private InterviewerCueType cueType; // → cue_type
}
