package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuestionFeedback {

  @JsonProperty("question_index")
  private int questionIndex;

  private String question;

  @JsonProperty("question_type")
  private String questionType;

  private Double percentage;

  // 기존 문자열 피드백(변경 없음). 새 구조는 detailedFeedback로 별도 추가된다.
  private String feedback;

  @JsonProperty("star_comment")
  private String starComment;

  @JsonProperty("voice_comment")
  private String voiceComment;

  // 신규: 문항별 구조화 피드백(없으면 응답 정규화에서 빈 객체 기본값).
  @JsonProperty("detailed_feedback")
  @JsonAlias("detailedFeedback")
  private QuestionDetailedFeedback detailedFeedback;

  // 신규: 기술 사실 검증(없으면 응답 정규화에서 미적용 + 빈 배열 기본값).
  @JsonProperty("fact_check")
  @JsonAlias("factCheck")
  private FactCheck factCheck;

  // AI 응답엔 없는 필드. 백엔드가 꼬리질문 여부를 주입(FE의 Q1-1 라벨링용).
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