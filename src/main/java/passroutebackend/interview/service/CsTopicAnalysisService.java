package passroutebackend.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.interview.dto.response.CsTopicAnalysisResponse;
import passroutebackend.interview.dto.response.CsTopicAnalysisResponse.TopicStat;
import passroutebackend.interview.entity.CsTopic;
import passroutebackend.interview.entity.InterviewFormat;
import passroutebackend.interview.entity.InterviewType;
import passroutebackend.interview.repository.CsTopicStatProjection;
import passroutebackend.interview.repository.InterviewQuestionRepository;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CsTopicAnalysisService {

    private static final double WEAK_TOPIC_THRESHOLD = 70.0;
    private static final int MAX_WEAK_TOPICS = 3;

    private static final Map<CsTopic, String> STUDY_RECOMMENDATIONS = new EnumMap<>(CsTopic.class);

    static {
        STUDY_RECOMMENDATIONS.put(CsTopic.DATA_STRUCTURE, "배열, 연결리스트, 트리, 그래프 등 자료구조의 특징과 시간복잡도를 비교하며 정리해보세요.");
        STUDY_RECOMMENDATIONS.put(CsTopic.ALGORITHM, "정렬, 탐색, 동적 계획법 등 주요 알고리즘의 동작 원리와 활용 사례를 복습해보세요.");
        STUDY_RECOMMENDATIONS.put(CsTopic.NETWORK, "TCP/IP, HTTP, OSI 7계층 등 네트워크 기본 개념과 통신 흐름을 다시 정리해보세요.");
        STUDY_RECOMMENDATIONS.put(CsTopic.OS, "프로세스와 스레드, 스케줄링, 메모리 관리 등 운영체제 핵심 개념을 복습해보세요.");
        STUDY_RECOMMENDATIONS.put(CsTopic.DATABASE, "인덱스, 트랜잭션, 정규화 등 데이터베이스 핵심 개념과 SQL 최적화를 학습해보세요.");
        STUDY_RECOMMENDATIONS.put(CsTopic.CONCURRENCY, "동시성 제어, 락, 데드락 등 멀티스레드 프로그래밍 개념을 다시 정리해보세요.");
        STUDY_RECOMMENDATIONS.put(CsTopic.MEMORY_GC, "메모리 구조와 가비지 컬렉션 동작 방식을 언어별로 비교하며 학습해보세요.");
        STUDY_RECOMMENDATIONS.put(CsTopic.LANGUAGE, "사용 언어의 핵심 문법과 동작 원리를 깊이 있게 복습해보세요.");
        STUDY_RECOMMENDATIONS.put(CsTopic.FRAMEWORK, "주요 프레임워크의 동작 원리와 생명주기를 다시 정리해보세요.");
        STUDY_RECOMMENDATIONS.put(CsTopic.DESIGN_PATTERN, "주요 디자인 패턴의 목적과 적용 사례를 예제와 함께 복습해보세요.");
        STUDY_RECOMMENDATIONS.put(CsTopic.SECURITY, "인증/인가, 암호화, 주요 보안 취약점과 대응 방안을 학습해보세요.");
    }

    private final InterviewQuestionRepository interviewQuestionRepository;

    @Transactional(readOnly = true)
    public CsTopicAnalysisResponse getAnalysis(Long userId) {
        List<CsTopicStatProjection> stats = interviewQuestionRepository
                .aggregateCsTopicStats(userId, InterviewType.TECHNICAL, InterviewFormat.ONE_ON_ONE);

        List<TopicStat> topics = stats.stream()
                .map(this::toTopicStat)
                .sorted(Comparator.comparingDouble(TopicStat::getAveragePercentage))
                .toList();

        List<TopicStat> weakTopics = topics.stream()
                .filter(topic -> topic.getAveragePercentage() < WEAK_TOPIC_THRESHOLD)
                .limit(MAX_WEAK_TOPICS)
                .toList();

        return new CsTopicAnalysisResponse(topics, weakTopics);
    }

    private TopicStat toTopicStat(CsTopicStatProjection projection) {
        CsTopic topic = projection.getCsTopic();
        double averagePercentage = round(projection.getAvgPercentage());

        return new TopicStat(
                topic,
                projection.getQuestionCount(),
                averagePercentage,
                STUDY_RECOMMENDATIONS.get(topic));
    }

    private double round(Double value) {
        if (value == null) {
            return 0.0;
        }
        return Math.round(value * 10) / 10.0;
    }
}
