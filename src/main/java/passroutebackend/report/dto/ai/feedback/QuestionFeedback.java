package passroutebackend.report.dto.ai.feedback;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "질문별 피드백")
@Getter
@NoArgsConstructor
public class QuestionFeedback {

  @Schema(description = "질문 순번", example = "1")
  @JsonProperty("question_index")
  private int questionIndex;

  @Schema(description = "질문 텍스트", example = "프로젝트에서 가장 어려웠던 점은?")
  private String question;

  @Schema(description = "질문 유형", example = "technical")
  @JsonProperty("question_type")
  private String questionType;

  @Schema(description = "점수 (백분율)", example = "75.0")
  private Double percentage;

  @Schema(description = "피드백 요약")
  private String feedback;

  @Schema(description = "STAR 기법 코멘트")
  @JsonProperty("star_comment")
  private String starComment;

  @Schema(description = "음성 관련 코멘트")
  @JsonProperty("voice_comment")
  private String voiceComment;

  @Schema(description = "구조화 상세 피드백")
  @JsonProperty("detailed_feedback")
  @JsonAlias("detailedFeedback")
  private QuestionDetailedFeedback detailedFeedback;

  @Schema(description = "기술 사실 검증 결과")
  @JsonProperty("fact_check")
  @JsonAlias("factCheck")
  private FactCheck factCheck;

  @Schema(description = "꼬리질문 여부", example = "false")
  @JsonProperty("follow_up")
  private boolean followUp;

  public void setFollowUp(boolean followUp) {
    this.followUp = followUp;
  }

  /**
   * 조회 응답 직전 호출. 새 필드가 없는(과거) 리포트나 인성 문항도 FE가 일관된 구조로 받도록
   * 안전 기본값을 채운다: detailedFeedback 빈 객체 보장, factCheck 미적용 + 빈 배열 보장.
   */
  public void normalizeForResponse() {
    if (detailedFeedback == null) {
      detailedFeedback = new QuestionDetailedFeedback();
    }
    detailedFeedback.normalize();
    if (factCheck == null) {
      factCheck = FactCheck.empty();
    }
    factCheck.normalize();
  }
}