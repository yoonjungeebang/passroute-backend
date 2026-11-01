package passroutebackend.interview.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "답변 제출 요청")
@Getter
@AllArgsConstructor
public class AnswerSubmitRequest {

  @Schema(description = "질문 ID")
  @NotNull
  private final Long questionId;

  @Schema(description = "답변 텍스트")
  @NotBlank
  private final String answerText;

  @Schema(description = "음성 분석 데이터")
  private final VoiceData voiceData;

  @Schema(description = "영상 URL")
  private final String videoUrl;

  @Schema(description = "클립 점수")
  private final Double clipScore;

  @Schema(description = "클립 선정 사유")
  private final String clipReason;
}
