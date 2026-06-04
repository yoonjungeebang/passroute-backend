package passroutebackend.selfintro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.global.exception.CustomException;
import passroutebackend.global.exception.ErrorCode;
import passroutebackend.selfintro.dto.SelfIntroRequestDto;
import passroutebackend.selfintro.dto.SelfIntroResponseDto;
import passroutebackend.selfintro.entity.SelfIntro;
import passroutebackend.selfintro.entity.SelfIntroItem;
import passroutebackend.selfintro.repository.SelfIntroRepository;
import passroutebackend.user.entity.User;
import passroutebackend.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SelfIntroService {

    private final SelfIntroRepository selfIntroRepository;
    private final UserRepository userRepository;

    // ── 목록 조회 ─────────────────────────────────────────────

    public List<SelfIntroResponseDto> getList(Long userId, String filter) {
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
                .map(SelfIntroResponseDto::from)
                .collect(Collectors.toList());
    }

    // ── 단건 조회 ─────────────────────────────────────────────

    public SelfIntroResponseDto getOne(Long userId, Long selfIntroId) {
        User user = getUser(userId);
        SelfIntro selfIntro = getSelfIntro(selfIntroId, user);
        return SelfIntroResponseDto.from(selfIntro);
    }

    // ── 생성 ─────────────────────────────────────────────────

    @Transactional
    public SelfIntroResponseDto create(Long userId, SelfIntroRequestDto dto) {
        User user = getUser(userId);
        SelfIntro selfIntro = SelfIntro.create(user, dto);
        selfIntro.updateItems(buildItems(selfIntro, dto.getItems()));
        selfIntroRepository.save(selfIntro);
        return SelfIntroResponseDto.from(selfIntro);
    }

    // ── 수정 ─────────────────────────────────────────────────

    @Transactional
    public SelfIntroResponseDto update(Long userId, Long selfIntroId, SelfIntroRequestDto dto) {
        User user = getUser(userId);
        SelfIntro selfIntro = getSelfIntro(selfIntroId, user);
        selfIntro.update(dto);
        selfIntro.updateItems(buildItems(selfIntro, dto.getItems()));
        return SelfIntroResponseDto.from(selfIntro);
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
                                           List<SelfIntroRequestDto.ItemDto> dtos) {
        List<SelfIntroItem> items = new ArrayList<>();
        for (int i = 0; i < dtos.size(); i++) {
            SelfIntroRequestDto.ItemDto dto = dtos.get(i);
            items.add(SelfIntroItem.create(selfIntro, dto.getQuestionText(), dto.getAnswerText(), i));
        }
        return items;
    }
}