package passroutebackend.interview.dto.debate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 토론 DTO의 snake_case ↔ camelCase 변환 검증.
 *
 * - 백엔드는 camelCase 필드 사용
 * - AI 서버는 snake_case JSON 사용
 * - @JsonNaming(SnakeCaseStrategy)가 자동 변환을 수행해야 함
 */
class DebateDtoSerializationTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
  }

  // ── 직렬화 (백엔드 → AI 서버) ───────────────────────────────────────────────

  @Nested
  @DisplayName("Request DTO 직렬화 (camelCase → snake_case)")
  class RequestSerialization {

    @Test
    @DisplayName("PersonaPayload — persona_id, debate_style, system_prompt_template")
    void serializePersonaPayload() throws Exception {
      PersonaPayload payload = PersonaPayload.builder()
          .personaId("persona_01_stable")
          .name("김지원")
          .background("3년차 백엔드")
          .debateStyle("COOPERATIVE")
          .difficulty("NORMAL")
          .strengths(List.of("논리적"))
          .weaknesses(List.of("보수적"))
          .systemPromptTemplate("당신은 ...")
          .build();

      String json = objectMapper.writeValueAsString(payload);

      assertThat(json).contains("\"persona_id\":\"persona_01_stable\"");
      assertThat(json).contains("\"debate_style\":\"COOPERATIVE\"");
      assertThat(json).contains("\"system_prompt_template\":\"당신은 ...\"");
      assertThat(json).doesNotContain("personaId");
      assertThat(json).doesNotContain("debateStyle");
    }

    @Test
    @DisplayName("DebateTurnItem — speaker_type, round_type")
    void serializeDebateTurnItem() throws Exception {
      DebateTurnItem item = DebateTurnItem.builder()
          .speakerType("USER")
          .roundType("REBUTTAL_1")
          .stance("PRO")
          .content("내 주장은...")
          .build();

      String json = objectMapper.writeValueAsString(item);

      assertThat(json).contains("\"speaker_type\":\"USER\"");
      assertThat(json).contains("\"round_type\":\"REBUTTAL_1\"");
      assertThat(json).doesNotContain("speakerType");
      assertThat(json).doesNotContain("roundType");
    }

    @Test
    @DisplayName("InterviewerOpeningRequest — topic_title, user_stance, pro_key_points")
    void serializeInterviewerOpeningRequest() throws Exception {
      InterviewerOpeningRequest request = InterviewerOpeningRequest.builder()
          .topicTitle("AI 면접 도입")
          .topicDescription("효율 vs 인간성")
          .userStance("PRO")
          .aiStance("CON")
          .difficulty("NORMAL")
          .proKeyPoints(List.of("효율"))
          .conKeyPoints(List.of("편향"))
          .build();

      String json = objectMapper.writeValueAsString(request);

      assertThat(json).contains("\"topic_title\":\"AI 면접 도입\"");
      assertThat(json).contains("\"user_stance\":\"PRO\"");
      assertThat(json).contains("\"pro_key_points\":[\"효율\"]");
      assertThat(json).contains("\"con_key_points\":[\"편향\"]");
    }

    @Test
    @DisplayName("DebateRebuttalRequest — rebuttal_round, opponent_latest_turn, persona nested")
    void serializeDebateRebuttalRequest() throws Exception {
      PersonaPayload persona = PersonaPayload.builder()
          .personaId("persona_02_aggressive")
          .name("박도현")
          .background("시니어")
          .debateStyle("AGGRESSIVE")
          .difficulty("HARD")
          .strengths(List.of())
          .weaknesses(List.of())
          .systemPromptTemplate("...")
          .build();

      DebateRebuttalRequest request = DebateRebuttalRequest.builder()
          .topicTitle("주제")
          .stance("CON")
          .difficulty("HARD")
          .persona(persona)
          .rebuttalRound("REBUTTAL_2")
          .opponentLatestTurn("사용자 발언")
          .history(List.of())
          .build();

      String json = objectMapper.writeValueAsString(request);

      assertThat(json).contains("\"rebuttal_round\":\"REBUTTAL_2\"");
      assertThat(json).contains("\"opponent_latest_turn\":\"사용자 발언\"");
      // nested persona 도 snake_case
      assertThat(json).contains("\"persona_id\":\"persona_02_aggressive\"");
    }
  }

  // ── 역직렬화 (AI 서버 → 백엔드) ────────────────────────────────────────────

  @Nested
  @DisplayName("Response DTO 역직렬화 (snake_case → camelCase)")
  class ResponseDeserialization {

    @Test
    @DisplayName("DebateTurnEvalResponse — 전체 활성 (REBUTTAL_1 시나리오)")
    void deserializeFullEvalResponse() throws Exception {
      String json = """
          {
            "scores": {
              "logic":            {"score": 4, "weight": 0.35, "feedback": "논리 좋음"},
              "rebuttal_quality": {"score": 3, "weight": 0.30, "feedback": "반박 보통"},
              "consistency":      {"score": 5, "weight": 0.20, "feedback": "일관성 우수"},
              "attitude":         {"score": 4, "weight": 0.15, "feedback": "태도 좋음"}
            },
            "weighted_score": 78.0,
            "summary": {
              "strengths": "논리 명확",
              "improvements": "사례 보강"
            }
          }
          """;

      DebateTurnEvalResponse response = objectMapper.readValue(json, DebateTurnEvalResponse.class);

      assertThat(response.getWeightedScore()).isEqualTo(78.0);
      assertThat(response.getScores().getLogic().getScore()).isEqualTo(4);
      assertThat(response.getScores().getLogic().getWeight()).isEqualTo(0.35);
      assertThat(response.getScores().getRebuttalQuality().getScore()).isEqualTo(3);
      assertThat(response.getScores().getConsistency().getScore()).isEqualTo(5);
      assertThat(response.getScores().getAttitude().getScore()).isEqualTo(4);
      assertThat(response.getSummary().getStrengths()).isEqualTo("논리 명확");
    }

    @Test
    @DisplayName("DebateTurnEvalResponse — OPENING 라운드 (rebuttal_quality·consistency null)")
    void deserializeOpeningEvalResponse() throws Exception {
      String json = """
          {
            "scores": {
              "logic":            {"score": 5, "weight": 0.7, "feedback": "..."},
              "rebuttal_quality": null,
              "consistency":      null,
              "attitude":         {"score": 5, "weight": 0.3, "feedback": "..."}
            },
            "weighted_score": 100.0,
            "summary": {"strengths": "s", "improvements": "i"}
          }
          """;

      DebateTurnEvalResponse response = objectMapper.readValue(json, DebateTurnEvalResponse.class);

      assertThat(response.getScores().getLogic()).isNotNull();
      assertThat(response.getScores().getAttitude()).isNotNull();
      assertThat(response.getScores().getRebuttalQuality()).isNull();
      assertThat(response.getScores().getConsistency()).isNull();
    }

    @Test
    @DisplayName("DebateReportResponse — 모든 필드 역직렬화")
    void deserializeReportResponse() throws Exception {
      String json = """
          {
            "overall": "전반 평가",
            "strengths": "강점",
            "weaknesses": [{"item": "logic", "comment": "보완 필요"}],
            "improvements": "개선",
            "turn_feedback": [
              {"round_type": "OPENING", "weighted_score": 86.0, "feedback": "잘함"}
            ],
            "strategy_analysis": "전략",
            "recommended_topics": ["주제1", "주제2"],
            "final_advice": "조언",
            "debate_readiness_comment": "준비됨"
          }
          """;

      DebateReportResponse response = objectMapper.readValue(json, DebateReportResponse.class);

      assertThat(response.getOverall()).isEqualTo("전반 평가");
      assertThat(response.getWeaknesses()).hasSize(1);
      assertThat(response.getTurnFeedback()).hasSize(1);
      assertThat(response.getTurnFeedback().get(0).getRoundType()).isEqualTo("OPENING");
      assertThat(response.getTurnFeedback().get(0).getWeightedScore()).isEqualTo(86.0);
      assertThat(response.getStrategyAnalysis()).isEqualTo("전략");
      assertThat(response.getRecommendedTopics()).hasSize(2);
      assertThat(response.getDebateReadinessComment()).isEqualTo("준비됨");
    }

    @Test
    @DisplayName("DebateSessionSummaryResponse — turn_highlights nested")
    void deserializeSessionSummaryResponse() throws Exception {
      String json = """
          {
            "overall": "o",
            "strengths": "s",
            "improvements": "i",
            "strategy_feedback": "sf",
            "turn_highlights": [
              {"round_type": "OPENING", "highlight_type": "best", "comment": "최고"},
              {"round_type": "CLOSING", "highlight_type": "worst", "comment": "최저"}
            ]
          }
          """;

      DebateSessionSummaryResponse response = objectMapper.readValue(json, DebateSessionSummaryResponse.class);

      assertThat(response.getStrategyFeedback()).isEqualTo("sf");
      assertThat(response.getTurnHighlights()).hasSize(2);
      assertThat(response.getTurnHighlights().get(0).getHighlightType()).isEqualTo("best");
      assertThat(response.getTurnHighlights().get(1).getRoundType()).isEqualTo("CLOSING");
    }

    @Test
    @DisplayName("InterviewerOpeningResponse — 단순 content")
    void deserializeSimpleContentResponse() throws Exception {
      String json = "{\"content\": \"안녕하세요, 토론을 시작합니다.\"}";

      InterviewerOpeningResponse response = objectMapper.readValue(json, InterviewerOpeningResponse.class);

      assertThat(response.getContent()).isEqualTo("안녕하세요, 토론을 시작합니다.");
    }
  }
}
