package passroutebackend.interview.service;

public record EvaluationContext(
    Long answerId,
    String questionText,
    String answerText,
    String interviewType,
    String difficulty,
    String jobPosition,
    String companyName
) {}
