package passroutebackend.interview.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

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

  @JsonCreator
  public static Difficulty from(String value) {
    return Arrays.stream(values())
            .filter(d -> d.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown difficulty: " + value));
  }
}
