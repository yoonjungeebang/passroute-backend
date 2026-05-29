package passroutebackend.interview.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

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

  @JsonCreator
  public static InterviewType from(String value) {
    return Arrays.stream(values())
            .filter(t -> t.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown interview type: " + value));
  }
}
