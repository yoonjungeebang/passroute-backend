package passroutebackend.interview.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.interview.dto.evaluation.LlmScores;

@Getter
@AllArgsConstructor
public class QuestionAnswerData {

    private int questionIndex;
    private String questionText;
    private String answerText;
    private Double percentage;
    private Integer starScore;
    private String llmScoresJson;
    private LlmScores llmScores;
    private Double concisenessFinal;
    private boolean followUp;
}
