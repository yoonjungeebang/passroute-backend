package passroutebackend.interview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Schema(description = "세션 질문 목록 응답")
@Getter
@AllArgsConstructor
public class SessionQuestionListResponse {

  @Schema(description = "질문 목록")
  private List<QuestionItem> questions;
  @Schema(description = "면접관 영상 정보")
  private PersonaVideoResponse interviewer;
}
