package passroutebackend.selfintro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.schedule.entity.InterviewSchedule;
import passroutebackend.schedule.entity.ScheduleStatus;
import passroutebackend.schedule.repository.InterviewScheduleRepository;
import passroutebackend.selfintro.dto.request.SelfIntroRequest;
import passroutebackend.selfintro.dto.response.SelfIntroResponse;
import passroutebackend.selfintro.entity.SelfIntro;
import passroutebackend.selfintro.entity.SelfIntroItem;
import passroutebackend.selfintro.repository.SelfIntroRepository;
import passroutebackend.user.entity.User;
import passroutebackend.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SelfIntroService {

    private final SelfIntroRepository selfIntroRepository;
    private final UserRepository userRepository;
    private final InterviewScheduleRepository interviewScheduleRepository;

    // ── 목록 조회 ─────────────────────────────────────────────

    public List<SelfIntroResponse> getList(Long userId, String filter) {
        User user = getUser(userId);

        List<SelfIntro> list = switch (filter) {
            case "1month" -> selfIntroRepository
                    .findByUserAndIsActiveTrueAndUpdatedAtAfterOrderByUpdatedAtDesc(
                            user, LocalDateTime.now().minusMonths(1));
            case "2month" -> selfIntroRepository
                    .findByUserAndIsActiveTrueAndUpdatedAtAfterOrderByUpdatedAtDesc(
                            user, LocalDateTime.now().minusMonths(2));
            case "scheduled" -> selfIntroRepository
                    .findByUserAndIsActiveTrueAndInterviewDateIsNotNullOrderByUpdatedAtDesc(user);
            case "unscheduled" -> selfIntroRepository
                    .findByUserAndIsActiveTrueAndInterviewDateIsNullOrderByUpdatedAtDesc(user);
            default -> selfIntroRepository.findByUserAndIsActiveTrueOrderByUpdatedAtDesc(user);
        };

        return list.stream()
                .map(SelfIntroResponse::from)
                .collect(Collectors.toList());
    }

    // ── 단건 조회 ─────────────────────────────────────────────

    public SelfIntroResponse getOne(Long userId, Long selfIntroId) {
        User user = getUser(userId);
        SelfIntro selfIntro = getSelfIntro(selfIntroId, user);
        return SelfIntroResponse.from(selfIntro);
    }

    // ── 생성 ─────────────────────────────────────────────────

    @Transactional
    public SelfIntroResponse create(Long userId, SelfIntroRequest dto) {
        User user = getUser(userId);
        SelfIntro selfIntro = SelfIntro.create(user, dto);
        selfIntro.updateItems(buildItems(selfIntro, dto.getItems()));
        selfIntroRepository.save(selfIntro);
        if (dto.getInterviewDate() != null) {
            createPendingSchedule(userId, selfIntro, dto.getInterviewDate(), dto.getInterviewTime());
        }
        return SelfIntroResponse.from(selfIntro);
    }

    // ── 수정 ─────────────────────────────────────────────────

    @Transactional
    public SelfIntroResponse update(Long userId, Long selfIntroId, SelfIntroRequest dto) {
        User user = getUser(userId);
        SelfIntro selfIntro = getSelfIntro(selfIntroId, user);
        selfIntro.update(dto);
        selfIntro.updateItems(buildItems(selfIntro, dto.getItems()));
        syncPendingSchedule(userId, selfIntro, dto.getInterviewDate(), dto.getInterviewTime());
        return SelfIntroResponse.from(selfIntro);
    }

    // ── 삭제 (소프트 딜리트) ──────────────────────────────────

    @Transactional
    public void delete(Long userId, Long selfIntroId) {
        User user = getUser(userId);
        SelfIntro selfIntro = getSelfIntro(selfIntroId, user);
        selfIntro.softDelete();
    }

    // ── private helpers ──────────────────────────────────────

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> CustomException.of(ErrorCode.USER_NOT_FOUND));  // 수정
    }

    private SelfIntro getSelfIntro(Long selfIntroId, User user) {
        return selfIntroRepository.findByIdAndUserAndIsActiveTrue(selfIntroId, user)
                .orElseThrow(() -> CustomException.of(ErrorCode.SELF_INTRO_NOT_FOUND));  // 수정
    }

    private List<SelfIntroItem> buildItems(SelfIntro selfIntro,
                                           List<SelfIntroRequest.ItemDto> dtos) {
        List<SelfIntroItem> items = new ArrayList<>();
        for (int i = 0; i < dtos.size(); i++) {
            SelfIntroRequest.ItemDto dto = dtos.get(i);
            items.add(SelfIntroItem.create(selfIntro, dto.getQuestionText(), dto.getAnswerText(), i));
        }
        return items;
    }

    private void syncPendingSchedule(Long userId, SelfIntro selfIntro, LocalDate newDate, String newTime) {
        Optional<InterviewSchedule> existing = interviewScheduleRepository
                .findBySelfIntroIdAndStatus(selfIntro.getId(), ScheduleStatus.PENDING);

        if (newDate == null) {
            existing.ifPresent(interviewScheduleRepository::delete);
            return;
        }

        if (interviewScheduleRepository.existsBySelfIntroIdAndStatus(selfIntro.getId(), ScheduleStatus.SCHEDULED)) {
            return;
        }

        LocalDateTime dateTime = toLocalDateTime(newDate, newTime);
        if (existing.isPresent()) {
            InterviewSchedule schedule = existing.get();
            schedule.update(schedule.getTitle(), schedule.getCompanyName(), schedule.getJobPosition(),
                    dateTime, schedule.getLocation(), schedule.getMemo());
        } else {
            createPendingSchedule(userId, selfIntro, newDate, newTime);
        }
    }

    private void createPendingSchedule(Long userId, SelfIntro selfIntro, LocalDate date, String time) {
        String title = selfIntro.getCompanyName() + " " + selfIntro.getJobPosition() + " 면접";
        InterviewSchedule schedule = InterviewSchedule.builder()
                .userId(userId)
                .selfIntroId(selfIntro.getId())
                .title(title)
                .companyName(selfIntro.getCompanyName())
                .jobPosition(selfIntro.getJobPosition())
                .interviewDate(toLocalDateTime(date, time))
                .status(ScheduleStatus.PENDING)
                .build();
        interviewScheduleRepository.save(schedule);
    }

    private LocalDateTime toLocalDateTime(LocalDate date, String time) {
        if (time == null || time.isBlank()) {
            return date.atStartOfDay();
        }
        String[] parts = time.split(":");
        return date.atTime(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}