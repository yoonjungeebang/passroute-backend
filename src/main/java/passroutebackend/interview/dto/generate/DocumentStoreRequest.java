package passroutebackend.interview.dto.generate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentStoreRequest {

  @JsonProperty("user_id")
  private final String userId;

  @JsonProperty("text")
  private final String text;

  @JsonProperty("doc_type")
  private final String docType;
}
