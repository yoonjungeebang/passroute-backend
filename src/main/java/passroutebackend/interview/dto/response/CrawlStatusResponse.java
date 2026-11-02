package passroutebackend.interview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "크롤링 상태 응답")
@Getter
@AllArgsConstructor
public class CrawlStatusResponse {

    @Schema(description = "크롤링 상태", example = "COMPLETED")
    private String status;
}
