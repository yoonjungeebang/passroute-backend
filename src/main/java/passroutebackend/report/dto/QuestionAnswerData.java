package passroutebackend.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import passroutebackend.report.dto.ai.score.LlmScores;

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
