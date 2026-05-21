package passroutebackend.interview.service;

public record QuestionAnswerData(
    int questionIndex,
    String questionText,
    String answerText,
    Double percentage,
    Integer starScore,
    String llmScoresJson,
    Double concisenessFinal
) {}
