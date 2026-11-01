package passroutebackend.interview.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReportContext {

    private Long sessionId;
    private String jobTitle;
    private String companyName;
    private String interviewType;
    private List<QuestionAnswerData> questionAnswers;
}
