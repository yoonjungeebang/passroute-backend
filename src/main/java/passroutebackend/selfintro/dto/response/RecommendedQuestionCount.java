package passroutebackend.selfintro.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "추천 질문 빈도")
@Getter
@AllArgsConstructor
public class RecommendedQuestionCount {
  @Schema(description = "추천 질문 텍스트", example = "프로젝트에서 맡은 역할을 설명해주세요.")
  private String text;
  @Schema(description = "추천 횟수", example = "3")
  private int count;
}
