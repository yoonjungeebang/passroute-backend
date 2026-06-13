package passroutebackend.interview.entity;

import java.util.Arrays;

// AI 서버(passroute-ai)의 CS_TOPICS와 값 동기화 필요
public enum CsTopic {
  DATA_STRUCTURE,
  ALGORITHM,
  NETWORK,
  OS,
  DATABASE,
  CONCURRENCY,
  MEMORY_GC,
  LANGUAGE,
  FRAMEWORK,
  DESIGN_PATTERN,
  SECURITY;

  // AI 응답값이 목록에 없거나 null이면 null 반환 (인성/토론 면접 질문 등)
  public static CsTopic fromOrNull(String value) {
    if (value == null) {
      return null;
    }
    return Arrays.stream(values())
        .filter(topic -> topic.name().equalsIgnoreCase(value))
        .findFirst()
        .orElse(null);
  }
}
