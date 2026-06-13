package passroutebackend.interview.dto.report;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * AI 평가 응답 → QuestionFeedback 매핑/저장/조회 라운드트립 검증.
 * 확정 스펙: 기존 feedback(string) 유지 + detailed_feedback/fact_check 별도 추가(모두 nullable).
 */
@DisplayName("QuestionFeedback 매핑")
class QuestionFeedbackMappingTest {

  private final ObjectMapper om = new ObjectMapper();

  private QuestionFeedback read(String json) throws Exception {
    return om.readValue(json, QuestionFeedback.class);
  }

  @Nested
  @DisplayName("신규 구조(detailed_feedback / fact_check)")
  class NewStructure {

    @Test
    @DisplayName("60점 미만 기술 문항 - detailed_feedback + fact_check를 누락 없이 매핑한다")
    void mapsTechnicalLowScore() throws Exception {
      String json = """
          {
            "question_index": 1,
            "question": "GET과 POST의 차이는?",
            "question_type": "technical",
            "percentage": 48,
            "feedback": "GET/POST 용도를 반대로 설명했습니다.",
            "star_comment": null,
            "voice_comment": "필러워드 5회 감지",
            "detailed_feedback": {
              "strength": "HTTP 메서드 개념은 인지",
              "weakness": "용도를 반대로 설명",
              "missing_info": ["멱등성", "캐시 가능 여부"],
              "improvement_example": "GET은 조회, POST는 생성에 사용한다고 설명",
              "suggested_answer": "GET은 조회하는 멱등 메서드, POST는 생성하는 비멱등 메서드입니다.",
              "retry_strategy": "용도 → 멱등성 → 캐시 순으로 정리 후 답변"
            },
            "fact_check": {
              "is_fact_check_applicable": true,
              "incorrect_claims": [
                { "user_claim": "GET은 생성, POST는 조회", "issue": "용도 반대",
                  "correct_explanation": "GET은 조회, POST는 생성", "suggested_fix": "반대로 정정" }
              ],
              "unsupported_claims": ["근거 없이 더 빠르다고 단정"]
            }
          }
          """;

      QuestionFeedback qf = read(json);

      // 기존 문자열 feedback 유지
      assertThat(qf.getFeedback()).isEqualTo("GET/POST 용도를 반대로 설명했습니다.");
      assertThat(qf.getVoiceComment()).isEqualTo("필러워드 5회 감지");

      // detailed_feedback
      assertThat(qf.getDetailedFeedback().getStrength()).isEqualTo("HTTP 메서드 개념은 인지");
      assertThat(qf.getDetailedFeedback().getMissingInfo()).containsExactly("멱등성", "캐시 가능 여부");
      assertThat(qf.getDetailedFeedback().getImprovementExample()).isNotNull();
      assertThat(qf.getDetailedFeedback().getSuggestedAnswer()).isNotNull();
      assertThat(qf.getDetailedFeedback().getRetryStrategy()).isNotNull();

      // fact_check
      assertThat(qf.getFactCheck().isFactCheckApplicable()).isTrue();
      assertThat(qf.getFactCheck().getIncorrectClaims()).hasSize(1);
      assertThat(qf.getFactCheck().getIncorrectClaims().get(0).getUserClaim()).isEqualTo("GET은 생성, POST는 조회");
      assertThat(qf.getFactCheck().getUnsupportedClaims()).containsExactly("근거 없이 더 빠르다고 단정");
    }

    @Test
    @DisplayName("60점 이상 인성 문항 - improvement/suggested/retry는 null, fact_check 미적용")
    void mapsPersonalityHighScore() throws Exception {
      String json = """
          {
            "question_index": 2,
            "question": "협업 중 갈등 해결 경험은?",
            "question_type": "personality",
            "percentage": 85,
            "feedback": "상황과 행동이 구체적이었습니다.",
            "star_comment": "Result가 부족했습니다.",
            "detailed_feedback": {
              "strength": "갈등 상황과 본인 행동이 구체적",
              "weakness": "조율 결과가 추상적",
              "missing_info": ["정량적 변화"],
              "improvement_example": null,
              "suggested_answer": null,
              "retry_strategy": null
            },
            "fact_check": { "is_fact_check_applicable": false, "incorrect_claims": [], "unsupported_claims": [] }
          }
          """;

      QuestionFeedback qf = read(json);

      assertThat(qf.getStarComment()).isEqualTo("Result가 부족했습니다.");
      assertThat(qf.getDetailedFeedback().getImprovementExample()).isNull();
      assertThat(qf.getDetailedFeedback().getSuggestedAnswer()).isNull();
      assertThat(qf.getFactCheck().isFactCheckApplicable()).isFalse();
      assertThat(qf.getFactCheck().getIncorrectClaims()).isEmpty();
    }

    @Test
    @DisplayName("AI가 camelCase로 보내도 alias로 매핑한다")
    void acceptsCamelCase() throws Exception {
      String json = """
          {
            "question_index": 3,
            "detailedFeedback": { "strength": "좋음", "missingInfo": ["A"], "improvementExample": "예시" },
            "factCheck": {
              "isFactCheckApplicable": true,
              "incorrectClaims": [{ "userClaim": "c", "issue": "i", "suggestedFix": "f" }],
              "unsupportedClaims": ["u"]
            }
          }
          """;

      QuestionFeedback qf = read(json);

      assertThat(qf.getDetailedFeedback().getMissingInfo()).containsExactly("A");
      assertThat(qf.getDetailedFeedback().getImprovementExample()).isEqualTo("예시");
      assertThat(qf.getFactCheck().isFactCheckApplicable()).isTrue();
      assertThat(qf.getFactCheck().getIncorrectClaims().get(0).getUserClaim()).isEqualTo("c");
      assertThat(qf.getFactCheck().getUnsupportedClaims()).containsExactly("u");
    }
  }

  @Nested
  @DisplayName("하위호환")
  class BackwardCompat {

    @Test
    @DisplayName("새 필드가 전혀 없는 과거 응답도 예외 없이 매핑된다")
    void legacyResponseWithoutNewFields() throws Exception {
      String json = """
          { "question_index": 1, "question": "Q", "percentage": 80, "feedback": "good", "star_comment": "STAR ok" }
          """;

      QuestionFeedback qf = read(json);

      assertThat(qf.getFeedback()).isEqualTo("good");
      assertThat(qf.getStarComment()).isEqualTo("STAR ok");
      assertThat(qf.getDetailedFeedback()).isNull();
      assertThat(qf.getFactCheck()).isNull();
    }

    @Test
    @DisplayName("normalizeForResponse는 detailedFeedback/factCheck 기본값(미적용·빈 배열)을 채운다")
    void normalizeFillsDefaults() throws Exception {
      QuestionFeedback qf = read("{ \"question_index\": 1, \"feedback\": \"good\" }");

      qf.normalizeForResponse();

      assertThat(qf.getDetailedFeedback()).isNotNull();
      assertThat(qf.getDetailedFeedback().getStrength()).isEmpty();
      assertThat(qf.getDetailedFeedback().getMissingInfo()).isEmpty();
      assertThat(qf.getFactCheck()).isNotNull();
      assertThat(qf.getFactCheck().isFactCheckApplicable()).isFalse();
      assertThat(qf.getFactCheck().getIncorrectClaims()).isEmpty();
      assertThat(qf.getFactCheck().getUnsupportedClaims()).isEmpty();
    }
  }

  @Nested
  @DisplayName("저장→조회 라운드트립")
  class RoundTrip {

    @Test
    @DisplayName("리스트 직렬화 후 재역직렬화해도 새 필드가 보존되고, 출력은 snake_case 키를 쓴다")
    void serializeThenDeserializeKeepsFields() throws Exception {
      String aiJson = """
          [{
            "question_index": 1,
            "feedback": "텍스트",
            "detailed_feedback": { "strength": "s", "missing_info": ["m"] },
            "fact_check": { "is_fact_check_applicable": true, "incorrect_claims": [], "unsupported_claims": ["u"] }
          }]
          """;
      List<QuestionFeedback> parsed =
          om.readValue(aiJson, new TypeReference<List<QuestionFeedback>>() {});

      // 저장 단계: DB TEXT 컬럼에 들어갈 JSON
      String stored = om.writeValueAsString(parsed);
      assertThat(stored)
          .contains("detailed_feedback")
          .contains("fact_check")
          .contains("is_fact_check_applicable")
          .contains("missing_info")
          .contains("unsupported_claims");

      // 조회 단계: DB JSON 재역직렬화
      List<QuestionFeedback> reloaded =
          om.readValue(stored, new TypeReference<List<QuestionFeedback>>() {});

      assertThat(reloaded).hasSize(1);
      assertThat(reloaded.get(0).getFeedback()).isEqualTo("텍스트");
      assertThat(reloaded.get(0).getDetailedFeedback().getStrength()).isEqualTo("s");
      assertThat(reloaded.get(0).getDetailedFeedback().getMissingInfo()).containsExactly("m");
      assertThat(reloaded.get(0).getFactCheck().isFactCheckApplicable()).isTrue();
      assertThat(reloaded.get(0).getFactCheck().getUnsupportedClaims()).containsExactly("u");
    }
  }
}