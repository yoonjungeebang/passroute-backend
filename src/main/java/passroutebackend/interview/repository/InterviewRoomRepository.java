package passroutebackend.interview.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import passroutebackend.interview.entity.InterviewFormat;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewType;
import passroutebackend.interview.entity.RoomStatus;

public interface InterviewRoomRepository extends JpaRepository<InterviewRoom, Long> {

    @Query("SELECT r FROM InterviewRoom r " +
            "WHERE r.userId = :userId AND r.status = :status " +
            "AND (:type IS NULL OR r.interviewType = :type) " +
            "AND (:format IS NULL OR r.interviewFormat = :format) " +
            "ORDER BY r.updatedAt DESC")
    Page<InterviewRoom> findHistories(
            @Param("userId") Long userId,
            @Param("status") RoomStatus status,
            @Param("type") InterviewType type,
            @Param("format") InterviewFormat format,
            Pageable pageable);

    @Query("SELECT DISTINCT r FROM InterviewRoom r " +
            "LEFT JOIN InterviewSession s ON s.interviewRoom = r " +
            "LEFT JOIN InterviewQuestion q ON q.session = s " +
            "WHERE r.userId = :userId AND r.status = :status " +
            "AND (LOWER(r.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "  OR LOWER(r.jobPosition) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "  OR LOWER(q.questionText) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY r.updatedAt DESC")
    Page<InterviewRoom> searchByKeyword(
            @Param("userId") Long userId,
            @Param("status") RoomStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);
}
