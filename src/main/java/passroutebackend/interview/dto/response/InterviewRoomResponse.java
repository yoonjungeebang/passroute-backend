package passroutebackend.interview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "면접 방 생성 응답")
@Getter
@AllArgsConstructor
public class InterviewRoomResponse {

    @Schema(description = "생성된 면접 방 ID")
    private Long roomId;
}
