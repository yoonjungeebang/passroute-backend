package passroutebackend.debate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import passroutebackend.debate.entity.DebateStyle;
import passroutebackend.global.type.Difficulty;

import java.util.List;

@Schema(description = "AI 페르소나 응답")
@Getter
@Builder
@AllArgsConstructor
public class DebatePersonaResponse {

  @Schema(description = "페르소나 ID")
  private Long id;
  @Schema(description = "페르소나 키")
  private String personaKey;
  @Schema(description = "이름")
  private String name;
  @Schema(description = "배경 설명")
  private String background;
  @Schema(description = "토론 스타일")
  private DebateStyle debateStyle;
  @Schema(description = "난이도")
  private Difficulty difficulty;
  @Schema(description = "강점")
  private List<String> strengths;
  @Schema(description = "약점")
  private List<String> weaknesses;
  @Schema(description = "발화 영상 URL")
  private String speakingVideoUrl;
  @Schema(description = "대기 영상 URL")
  private String silenceVideoUrl;
}
