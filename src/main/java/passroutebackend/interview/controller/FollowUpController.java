package passroutebackend.interview.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import passroutebackend.global.ApiResponse;
import passroutebackend.interview.dto.AnswerSubmitRequest;
import passroutebackend.interview.dto.AnswerSubmitResponse;
import passroutebackend.interview.service.FollowUpService;

@RestController
@RequestMapping("/interviews")
@RequiredArgsConstructor
public class FollowUpController {

  private final FollowUpService followUpService;

  @PostMapping("/answers")
  public ApiResponse<AnswerSubmitResponse> submitAnswer(
      @Valid @RequestBody AnswerSubmitRequest request) {
    AnswerSubmitResponse response = followUpService.submitAnswer(request);
    return ApiResponse.success(response);
  }
}
