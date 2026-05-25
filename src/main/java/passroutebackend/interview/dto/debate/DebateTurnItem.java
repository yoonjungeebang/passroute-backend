package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 토론 히스토리/평가에 사용되는 단일 발화 항목.
 * AI 서버로 전달 시 SnakeCase로 자동 변환.
 */
@Getter
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DebateTurnItem {

  private String speakerType;   // USER / AI_COMPETITOR / AI_INTERVIEWER
  private String roundType;     // OPENING / REBUTTAL_1 / REBUTTAL_2 / CLOSING / MODERATION
  private String stance;        // PRO / CON / NEUTRAL
  private String content;
}
