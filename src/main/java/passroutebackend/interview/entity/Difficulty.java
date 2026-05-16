package passroutebackend.interview.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Difficulty {

  EASY("low"),
  NORMAL("middle"),
  HARD("high");

  private final String value;

  Difficulty(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
