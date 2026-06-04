package passroutebackend.schedule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.interview.service.InterviewRoomService;
import passroutebackend.schedule.dto.request.ScheduleCreateRequest;
import passroutebackend.schedule.dto.request.ScheduleStatusUpdateRequest;
import passroutebackend.schedule.dto.request.ScheduleUpdateRequest;
import passroutebackend.schedule.dto.response.ScheduleCalendarResponse;
import passroutebackend.schedule.dto.response.ScheduleResponse;
import passroutebackend.schedule.entity.InterviewSchedule;
import passroutebackend.schedule.entity.ScheduleStatus;
import passroutebackend.schedule.repository.InterviewScheduleRepository;
import passroutebackend.selfintro.entity.SelfIntro;
import passroutebackend.selfintro.repository.SelfIntroRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewScheduleService {

    private final InterviewScheduleRepository interviewScheduleRepository;
    private final InterviewRoomService interviewRoomService;
    private final SelfIntroRepository selfIntroRepository;

    @Transactional
    public ScheduleResponse create(Long userId, ScheduleCreateRequest request) {
        String title;
        String companyName;
        String jobPosition;
        Long selfIntroId = request.getSelfIntroId();

        if (selfIntroId != null) {
            SelfIntro selfIntro = selfIntroRepository
                    .findByIdAndUser_IdAndIsActiveTrue(selfIntroId, userId)
                    .orElseThrow(() -> CustomException.of(ErrorCode.SELF_INTRO_NOT_FOUND));

            companyName = selfIntro.getCompanyName();
            jobPosition = selfIntro.getJobPosition();
            title = companyName + " " + jobPosition + " 면접";

            LocalDate date = request.getInterviewDate().toLocalDate();
            String time = String.format("%02d:%02d",
                    request.getInterviewDate().getHour(),
                    request.getInterviewDate().getMinute());
            selfIntro.scheduleInterview(date, time);
        } else {
            if (request.getTitle() == null || request.getCompanyName() == null || request.getJobPosition() == null) {
                throw CustomException.of(ErrorCode.INVALID_INPUT);
            }
            title = request.getTitle();
            companyName = request.getCompanyName();
            jobPosition = request.getJobPosition();
        }

        InterviewSchedule schedule = InterviewSchedule.builder()
                .userId(userId)
                .selfIntroId(selfIntroId)
                .title(title)
                .companyName(companyName)
                .jobPosition(jobPosition)
                .interviewDate(request.getInterviewDate())
                .location(request.getLocation())
                .memo(request.getMemo())
                .status(ScheduleStatus.SCHEDULED)
                .build();

        interviewScheduleRepository.save(schedule);
        return ScheduleResponse.from(schedule);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getList(Long userId) {
        return interviewScheduleRepository.findByUserIdOrderByInterviewDateAsc(userId)
                .stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ScheduleCalendarResponse getCalendar(Long userId, int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusNanos(1);

        List<ScheduleResponse> schedules = interviewScheduleRepository
                .findByUserIdAndInterviewDateBetweenOrderByInterviewDateAsc(userId, start, end)
                .stream()
                .map(ScheduleResponse::from)
                .toList();

        return new ScheduleCalendarResponse(year, month, schedules);
    }

    @Transactional(readOnly = true)
    public ScheduleResponse getDetail(Long userId, Long scheduleId) {
        InterviewSchedule schedule = findScheduleByIdAndValidateOwner(userId, scheduleId);
        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public ScheduleResponse update(Long userId, Long scheduleId, ScheduleUpdateRequest request) {
        InterviewSchedule schedule = findScheduleByIdAndValidateOwner(userId, scheduleId);
        schedule.update(
                request.getTitle(),
                request.getCompanyName(),
                request.getJobPosition(),
                request.getInterviewDate(),
                request.getLocation(),
                request.getMemo()
        );
        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public void delete(Long userId, Long scheduleId) {
        InterviewSchedule schedule = findScheduleByIdAndValidateOwner(userId, scheduleId);
        interviewScheduleRepository.delete(schedule);
    }

    @Transactional
    public ScheduleResponse updateStatus(Long userId, Long scheduleId, ScheduleStatusUpdateRequest request) {
        InterviewSchedule schedule = findScheduleByIdAndValidateOwner(userId, scheduleId);
        schedule.updateStatus(request.getStatus());
        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public Long practice(Long userId, Long scheduleId) {
        InterviewSchedule schedule = findScheduleByIdAndValidateOwner(userId, scheduleId);
        return interviewRoomService.createPracticeRoom(
                userId, schedule.getCompanyName(), schedule.getJobPosition());
    }

    private InterviewSchedule findScheduleByIdAndValidateOwner(Long userId, Long scheduleId) {
        InterviewSchedule schedule = interviewScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> CustomException.of(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!schedule.getUserId().equals(userId)) {
            throw CustomException.of(ErrorCode.SCHEDULE_ACCESS_DENIED);
        }

        return schedule;
    }
}
