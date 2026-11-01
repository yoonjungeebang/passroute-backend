package passroutebackend.report.dto.ai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import passroutebackend.report.dto.ai.summary.EvaluationSummary;
import passroutebackend.report.dto.ai.feedback.FactCheck;
import passroutebackend.report.dto.ai.score.LlmScores;

@Getter
@NoArgsConstructor
public class QuestionEvaluationResponse {

  @JsonProperty("llm_scores")
  private LlmScores llmScores;

  private EvaluationSummary summary;

  // 신규(nullable): 답변별 기술 사실 검증. 현재는 받아만 두고 저장하지 않는다.
  // 리포트에 노출되는 fact_check는 /report/generate 응답(question_feedback[].fact_check)에서 생성됨.
  @JsonProperty("fact_check")
  private FactCheck factCheck;
}