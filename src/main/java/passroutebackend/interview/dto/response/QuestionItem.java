package passroutebackend.interview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "질문 항목")
@Getter
@AllArgsConstructor
public class QuestionItem {

  @Schema(description = "질문 ID")
  private Long questionId;
  @Schema(description = "질문 텍스트")
  private String questionText;
  @Schema(description = "질문 순서")
  private int questionOrder;
  @Schema(description = "질문 TTS 음성 URL")
  private String audioUrl;
}
