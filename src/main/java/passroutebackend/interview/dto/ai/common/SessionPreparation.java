package passroutebackend.interview.dto.ai.common;

import lombok.AllArgsConstructor;
import passroutebackend.interview.dto.ai.request.QuestionGenerateRequest;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SessionPreparation {

    private Long sessionId;
    private QuestionGenerateRequest aiRequest;
    private String aiInterviewer;
}
