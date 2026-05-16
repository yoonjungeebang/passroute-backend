package passroutebackend.interview.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum InterviewType {

  TECHNICAL("technical"),
  PERSONALITY("personality");

  private final String value;

  InterviewType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
