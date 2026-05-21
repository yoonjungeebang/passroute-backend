package passroutebackend.interview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import passroutebackend.interview.client.AiServerClient;
import passroutebackend.interview.dto.VoiceData;
import passroutebackend.interview.dto.evaluation.LlmScoreItem;
import passroutebackend.interview.dto.evaluation.LlmScores;
import passroutebackend.interview.dto.evaluation.QuestionEvaluationRequest;
import passroutebackend.interview.dto.evaluation.QuestionEvaluationResponse;
import passroutebackend.interview.dto.evaluation.StarEvaluationRequest;
import passroutebackend.interview.dto.evaluation.StarEvaluationResponse;
import passroutebackend.interview.entity.InterviewType;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationService {

  private final AiServerClient aiServerClient;
  private final EvaluationTransactionService evaluationTransactionService;
  private final ObjectMapper objectMapper;

  @Async("evaluationExecutor")
  public void evaluateAsync(Long questionId, VoiceData voiceData) {
    try {
      // 트랜잭션 안에서 Lazy 연관관계 탐색 후 단순 데이터로 반환
      EvaluationContext ctx = evaluationTransactionService.loadContext(questionId);

      QuestionEvaluationResponse evalResponse = aiServerClient.evaluateQuestion(
          new QuestionEvaluationRequest(
              ctx.jobPosition(),
              ctx.companyName(),
              List.of(),
              ctx.interviewType(),
              ctx.questionText(),
              ctx.answerText()
          )
      );

      StarEvaluationResponse starResponse = aiServerClient.evaluateStar(
          new StarEvaluationRequest(ctx.questionText(), ctx.answerText())
      );

      if (evalResponse == null || starResponse == null) {
        log.warn("AI 서버 응답이 null, questionId={}", questionId);
        return;
      }

      LlmScores llmScores = evalResponse.getLlmScores();
      double voicePenalty = calculateVoicePenalty(voiceData);
      double concisenessFinal = calculateConcisenessFinal(
          llmScores != null ? llmScores.getConciseness() : null, voicePenalty);
      double percentage = calculatePercentage(llmScores, concisenessFinal, ctx.interviewType());

      String llmScoresJson = toJson(evalResponse);
      evaluationTransactionService.saveResult(ctx.answerId(), percentage, starResponse.getStarScore(), llmScoresJson);

    } catch (Exception e) {
      log.warn("평가 처리 실패, questionId={}: {}", questionId, e.getMessage());
    }
  }

  // filler_word_count, wpm 기반 페널티 계산
  private double calculateVoicePenalty(VoiceData voiceData) {
    if (voiceData == null) {
      return 0.0;
    }
    double penalty = 0.0;
    if (voiceData.getFillerWordCount() != null) {
      if (voiceData.getFillerWordCount() >= 10) penalty -= 1.0;
      else if (voiceData.getFillerWordCount() >= 5) penalty -= 0.5;
    }
    if (voiceData.getWpm() != null) {
      double wpm = voiceData.getWpm();
      if (wpm < 100 || wpm > 180) penalty -= 0.5;
    }
    return penalty;
  }

  // conciseness_final = max(1.0, conciseness + voice_penalty)
  private double calculateConcisenessFinal(LlmScoreItem conciseness, double voicePenalty) {
    if (conciseness == null || conciseness.getScore() == null) {
      return 1.0;
    }
    return Math.max(1.0, conciseness.getScore() + voicePenalty);
  }

  // percentage = Σ(score × weight) / Σ(weight) / 5.0 × 100, null 항목 제외
  private double calculatePercentage(LlmScores scores, double concisenessFinal, String interviewType) {
    if (scores == null) return 0.0;
    boolean isTechnical = InterviewType.TECHNICAL.getValue().equals(interviewType);
    double weightedSum = 0.0;
    double totalWeight = 0.0;

    weightedSum += addScore(scores.getRelevance(), 0.15);
    totalWeight += weightOf(scores.getRelevance(), 0.15);

    double logicWeight = isTechnical ? 0.15 : 0.20;
    weightedSum += addScore(scores.getLogic(), logicWeight);
    totalWeight += weightOf(scores.getLogic(), logicWeight);

    weightedSum += addScore(scores.getSpecificity(), 0.15);
    totalWeight += weightOf(scores.getSpecificity(), 0.15);

    double concisenessWeight = isTechnical ? 0.10 : 0.15;
    if (scores.getConciseness() != null) {
      weightedSum += concisenessFinal * concisenessWeight;
      totalWeight += concisenessWeight;
    }

    double clarityWeight = isTechnical ? 0.10 : 0.15;
    weightedSum += addScore(scores.getClarity(), clarityWeight);
    totalWeight += weightOf(scores.getClarity(), clarityWeight);

    weightedSum += addScore(scores.getJobRelevance(), 0.05);
    totalWeight += weightOf(scores.getJobRelevance(), 0.05);

    if (isTechnical) {
      weightedSum += addScore(scores.getAccuracy(), 0.15);
      totalWeight += weightOf(scores.getAccuracy(), 0.15);
      weightedSum += addScore(scores.getDepth(), 0.15);
      totalWeight += weightOf(scores.getDepth(), 0.15);
    } else {
      weightedSum += addScore(scores.getAuthenticity(), 0.10);
      totalWeight += weightOf(scores.getAuthenticity(), 0.10);
      weightedSum += addScore(scores.getGrowth(), 0.05);
      totalWeight += weightOf(scores.getGrowth(), 0.05);
    }

    if (totalWeight == 0) return 0.0;
    return weightedSum / totalWeight / 5.0 * 100;
  }

  private double addScore(LlmScoreItem item, double weight) {
    return (item != null && item.getScore() != null) ? item.getScore() * weight : 0.0;
  }

  private double weightOf(LlmScoreItem item, double weight) {
    return (item != null && item.getScore() != null) ? weight : 0.0;
  }

  private String toJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      log.warn("LLM 점수 직렬화 실패: {}", e.getMessage());
      return null;
    }
  }
}
