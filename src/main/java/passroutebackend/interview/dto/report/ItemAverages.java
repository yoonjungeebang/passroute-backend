package passroutebackend.interview.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemAverages {

  private ItemAvg relevance;
  private ItemAvg logic;
  private ItemAvg specificity;
  private ItemAvg conciseness;
  private ItemAvg clarity;

  @JsonProperty("job_relevance")
  private ItemAvg jobRelevance;

  private ItemAvg accuracy;
  private ItemAvg depth;
  private ItemAvg authenticity;
  private ItemAvg growth;

  public Map<String, Double> toMap() {
    Map<String, Double> map = new LinkedHashMap<>();
    if (relevance != null) map.put("relevance", relevance.getAvg());
    if (logic != null) map.put("logic", logic.getAvg());
    if (specificity != null) map.put("specificity", specificity.getAvg());
    if (conciseness != null) map.put("conciseness", conciseness.getAvg());
    if (clarity != null) map.put("clarity", clarity.getAvg());
    if (jobRelevance != null) map.put("jobRelevance", jobRelevance.getAvg());
    if (accuracy != null) map.put("accuracy", accuracy.getAvg());
    if (depth != null) map.put("depth", depth.getAvg());
    if (authenticity != null) map.put("authenticity", authenticity.getAvg());
    if (growth != null) map.put("growth", growth.getAvg());
    return map;
  }
}
