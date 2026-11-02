package passroutebackend.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "면접 일정 생성 요청")
@Getter
@NoArgsConstructor
public class ScheduleCreateRequest {

    @Schema(description = "연결할 자소서 ID")
    private Long selfIntroId;

    @Schema(description = "일정 제목", example = "네이버 1차 면접")
    @Size(max = 100)
    private String title;

    @Schema(description = "기업명", example = "네이버")
    @Size(max = 100)
    private String companyName;

    @Schema(description = "직무", example = "백엔드 개발")
    @Size(max = 100)
    private String jobPosition;

    @Schema(description = "면접 일시")
    @NotNull
    private LocalDateTime interviewDate;

    @Schema(description = "면접 장소", example = "서울시 강남구")
    @Size(max = 200)
    private String location;

    @Schema(description = "메모")
    private String memo;
}
