package passroutebackend.interview.dto.debate;

/**
 * 면접관 진행 멘트(cue) 종류. AI /debate/interviewer-cue 요청의 cue_type 값.
 * 기본 Jackson 직렬화(name())로 "REBUTTAL_START" 등 대문자 그대로 전송된다.
 */
public enum InterviewerCueType {
  REBUTTAL_START,   // 양측 입론 후 반박 시작 안내
  REBUTTAL_EXTRA,   // '반박 한 번 더' 선택 후 추가 반박 안내
  CLOSING_GUIDE     // 마무리 발언 안내
}
