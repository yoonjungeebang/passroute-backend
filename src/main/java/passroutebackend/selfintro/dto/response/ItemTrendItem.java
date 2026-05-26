package passroutebackend.selfintro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemTrendItem {
  private String item;
  private double firstAvg;
  private double lastAvg;
  private double diff;
  private TrendDirection direction;
}
