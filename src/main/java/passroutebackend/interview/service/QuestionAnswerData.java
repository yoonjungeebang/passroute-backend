package passroutebackend.interview.service;

import passroutebackend.interview.dto.evaluation.LlmScores;

public record QuestionAnswerData(
    int questionIndex,
    String questionText,
    String answerText,
    Double percentage,
    Integer starScore,
    String llmScoresJson,
    LlmScores llmScores,
    Double concisenessFinal
) {}