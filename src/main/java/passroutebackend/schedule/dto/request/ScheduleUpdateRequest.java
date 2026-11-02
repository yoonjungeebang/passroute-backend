package passroutebackend.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "면접 일정 수정 요청")
@Getter
@NoArgsConstructor
public class ScheduleUpdateRequest {

    @Schema(description = "일정 제목")
    @NotBlank
    @Size(max = 100)
    private String title;

    @Schema(description = "기업명")
    @NotBlank
    @Size(max = 100)
    private String companyName;

    @Schema(description = "직무")
    @NotBlank
    @Size(max = 100)
    private String jobPosition;

    @Schema(description = "면접 일시")
    @NotNull
    private LocalDateTime interviewDate;

    @Schema(description = "면접 장소")
    @Size(max = 200)
    private String location;

    @Schema(description = "메모")
    private String memo;
}
