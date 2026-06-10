package passroutebackend.report.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import passroutebackend.report.dto.response.ReportListItem;

@Repository
@RequiredArgsConstructor
public class ReportListQueryRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  private static final String DATA_SQL = """
      (
        SELECT
          'interview' AS report_type,
          s.id AS domain_id,
          room.interview_type AS interview_type,
          room.company_name AS company_name,
          room.job_position AS job_position,
          s.ended_at AS date,
          ir.session_score AS total_score,
          LEFT(ir.overall, 50) AS feedback_preview,
          room.si_id AS self_intro_id
        FROM interview_reports ir
        JOIN interview_sessions s ON ir.session_id = s.id
        JOIN interview_rooms room ON s.room_id = room.id
        WHERE :includeInterview = 1
          AND room.user_id = :userId
          AND s.status = 'COMPLETED'
          AND s.is_active = true
          AND ir.report_status = 'COMPLETED'
          AND (:resumeId IS NULL OR room.si_id = :resumeId)
          AND (:interviewTypeFilter IS NULL OR room.interview_type = :interviewTypeFilter)
          AND (
            :q IS NULL
            OR LOWER(room.company_name) LIKE :q
            OR LOWER(room.job_position) LIKE :q
          )
      )
      UNION ALL
      (
        SELECT
          'debate' AS report_type,
          ds.id AS domain_id,
          NULL AS interview_type,
          NULL AS company_name,
          dt.title AS job_position,
          ds.ended_at AS date,
          dr.session_score AS total_score,
          LEFT(dr.overall, 50) AS feedback_preview,
          NULL AS self_intro_id
        FROM debate_reports dr
        JOIN debate_sessions ds ON dr.session_id = ds.id
        JOIN debate_topics dt ON ds.topic_id = dt.id
        WHERE :includeDebate = 1
          AND :resumeId IS NULL
          AND ds.user_id = :userId
          AND dr.report_status = 'COMPLETED'
          AND ds.ended_at IS NOT NULL
          AND (:q IS NULL OR LOWER(dt.title) LIKE :q)
      )
      ORDER BY date DESC, domain_id DESC
      LIMIT :size OFFSET :offset
      """;

  private static final String COUNT_SQL = """
      SELECT COUNT(*) FROM (
        (
          SELECT 1
          FROM interview_reports ir
          JOIN interview_sessions s ON ir.session_id = s.id
          JOIN interview_rooms room ON s.room_id = room.id
          WHERE :includeInterview = 1
            AND room.user_id = :userId
            AND s.status = 'COMPLETED'
            AND s.is_active = true
            AND ir.report_status = 'COMPLETED'
            AND (:resumeId IS NULL OR room.si_id = :resumeId)
            AND (:interviewTypeFilter IS NULL OR room.interview_type = :interviewTypeFilter)
            AND (
              :q IS NULL
              OR LOWER(room.company_name) LIKE :q
              OR LOWER(room.job_position) LIKE :q
            )
        )
        UNION ALL
        (
          SELECT 1
          FROM debate_reports dr
          JOIN debate_sessions ds ON dr.session_id = ds.id
          JOIN debate_topics dt ON ds.topic_id = dt.id
          WHERE :includeDebate = 1
            AND :resumeId IS NULL
            AND ds.user_id = :userId
            AND dr.report_status = 'COMPLETED'
            AND ds.ended_at IS NOT NULL
            AND (:q IS NULL OR LOWER(dt.title) LIKE :q)
        )
      ) AS combined
      """;

  public List<ReportListItem> findReports(ReportListQueryParams params, int page, int size) {
    MapSqlParameterSource src = buildParams(params)
        .addValue("size", size)
        .addValue("offset", (long) page * size);

    return jdbcTemplate.query(DATA_SQL, src, (rs, rowNum) -> {
      String interviewTypeRaw = rs.getString("interview_type");
      Long selfIntroId = rs.getLong("self_intro_id");
      if (rs.wasNull()) selfIntroId = null;
      return new ReportListItem(
          rs.getString("report_type"),
          rs.getLong("domain_id"),
          interviewTypeRaw == null ? null : interviewTypeRaw.toLowerCase(),
          rs.getString("company_name"),
          rs.getString("job_position"),
          rs.getTimestamp("date") != null ? rs.getTimestamp("date").toLocalDateTime() : null,
          rs.getDouble("total_score"),
          rs.getString("feedback_preview"),
          selfIntroId
      );
    });
  }

  public long count(ReportListQueryParams params) {
    MapSqlParameterSource src = buildParams(params);
    Long result = jdbcTemplate.queryForObject(COUNT_SQL, src, Long.class);
    return result != null ? result : 0L;
  }

  private MapSqlParameterSource buildParams(ReportListQueryParams p) {
    return new MapSqlParameterSource()
        .addValue("userId", p.getUserId())
        .addValue("includeInterview", p.isIncludeInterview() ? 1 : 0)
        .addValue("includeDebate", p.isIncludeDebate() ? 1 : 0)
        .addValue("interviewTypeFilter", p.getInterviewTypeFilter())
        .addValue("resumeId", p.getResumeId())
        .addValue("q", p.getQ());
  }
}
