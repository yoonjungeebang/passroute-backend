package passroutebackend.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SaveWorstClipRequest {

  @NotNull
  private final Long questionId;

  @NotBlank
  private final String videoUrl;

  @NotNull
  private final Double clipScore;

  private final String clipReason;
}
