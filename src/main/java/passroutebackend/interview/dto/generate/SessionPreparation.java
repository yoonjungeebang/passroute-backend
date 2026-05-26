package passroutebackend.interview.dto.generate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SessionPreparation {

    private Long sessionId;
    private QuestionGenerateRequest aiRequest;
}
