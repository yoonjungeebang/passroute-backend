package passroutebackend.interview.service;

import java.util.List;

public record ReportContext(
    Long sessionId,
    String jobTitle,
    String companyName,
    String interviewType,
    List<QuestionAnswerData> questionAnswers
) {}
