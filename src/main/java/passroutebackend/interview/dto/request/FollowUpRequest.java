package passroutebackend.interview.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import passroutebackend.interview.dto.ai.common.QATurn;

import java.util.List;

@Schema(description = "꼬리질문 생성 요청 (AI 서버 전달용)")
@Getter
@AllArgsConstructor
public class FollowUpRequest {

  @Schema(description = "면접 유형", example = "TECHNICAL")
  @JsonProperty("interview_type")
  private final String interviewType;

  @Schema(description = "난이도", example = "MEDIUM")
  @JsonProperty("difficulty")
  private final String difficulty;

  @Schema(description = "이전 질문-답변 대화 이력")
  @JsonProperty("conversation")
  private final List<QATurn> conversation;

  @Schema(description = "사용자 ID", example = "1")
  @JsonProperty("user_id")
  private final Long userId;

  @Schema(description = "면접관 페르소나 (원질문과 같은 TTS 유지용)", example = "HR_MANAGER")
  @JsonProperty("persona")
  private final String persona;
}
