package passroutebackend.debate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import passroutebackend.debate.entity.DebateRound;
import passroutebackend.debate.entity.DebateSession;
import passroutebackend.debate.entity.DebateTurn;
import passroutebackend.debate.entity.SpeakerType;

import java.util.List;

public interface DebateTurnRepository extends JpaRepository<DebateTurn, Long> {

  // 세션의 전체 턴 시간순 조회 (히스토리 빌드용)
  List<DebateTurn> findBySessionOrderByCreatedAtAsc(DebateSession session);

  // AI 경쟁자 발화만 시간순 조회 (세션 요약 ai_competitor_turns 정렬용)
  List<DebateTurn> findBySessionAndSpeakerTypeOrderByCreatedAtAsc(
      DebateSession session, SpeakerType speakerType);

  // 특정 라운드의 발화 존재 여부 (PRACTICE 확정 시 제출된 시도 존재 검증용)
  boolean existsBySessionAndSpeakerTypeAndRound(
      DebateSession session, SpeakerType speakerType, DebateRound round);

  // 특정 라운드의 발화 삭제 (PRACTICE 재시도 시 이전 시도 교체용)
  // 라운드당 USER 발화는 최대 1건이지만, SELECT 없이 단일 DELETE로 처리한다.
  @Modifying
  @Query("delete from DebateTurn t where t.session = :session "
      + "and t.speakerType = :speakerType and t.round = :round")
  void deleteBySessionAndSpeakerTypeAndRound(
      @Param("session") DebateSession session,
      @Param("speakerType") SpeakerType speakerType,
      @Param("round") DebateRound round);
}
