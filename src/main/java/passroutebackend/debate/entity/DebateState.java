package passroutebackend.debate.entity;

public enum DebateState {
  CREATED,
  INTERVIEWER_OPENING,
  OPENING_USER,
  OPENING_AI,
  INTERVIEWER_REBUTTAL_CUE,    // 면접관: 반박 시작 안내 (양측 입론 후) → cue_type REBUTTAL_START
  REBUTTAL_1_USER,
  REBUTTAL_1_AI,
  REBUTTAL_1_DECISION,         // 사용자 선택 대기 ('반박 한 번 더' / '토론 마무리')
  INTERVIEWER_REBUTTAL2_CUE,   // 면접관: 추가 반박 안내 ('반박 한 번 더' 선택 후) → cue_type REBUTTAL_EXTRA
  REBUTTAL_2_USER,
  REBUTTAL_2_AI,
  INTERVIEWER_CLOSING_CUE,     // 면접관: 마무리 발언 안내 (마무리 진입 직전) → cue_type CLOSING_GUIDE
  CLOSING_USER,
  CLOSING_AI,
  INTERVIEWER_CLOSING,
  FINISHED
}
