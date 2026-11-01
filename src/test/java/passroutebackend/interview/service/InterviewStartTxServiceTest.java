package passroutebackend.interview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import passroutebackend.document.repository.DocumentAnalysisRepository;
import passroutebackend.document.repository.DocumentRepository;
import passroutebackend.interview.dto.ai.common.SessionPreparation;
import passroutebackend.interview.dto.ai.request.QuestionGenerateRequest.CrawledData;
import passroutebackend.interview.entity.CrawlRequestStatus;
import passroutebackend.interview.entity.InterviewFormat;
import passroutebackend.interview.entity.InterviewRoom;
import passroutebackend.interview.entity.InterviewSession;
import passroutebackend.interview.entity.InterviewType;
import passroutebackend.interview.entity.JobDescription;
import passroutebackend.interview.entity.RoomCrawlRequest;
import passroutebackend.interview.entity.RoomStatus;
import passroutebackend.interview.repository.InterviewQuestionRepository;
import passroutebackend.interview.repository.InterviewRoomRepository;
import passroutebackend.interview.repository.InterviewSessionRepository;
import passroutebackend.interview.repository.JobDescriptionRepository;
import passroutebackend.interview.repository.RoomCrawlRequestRepository;
import passroutebackend.global.type.Difficulty;
import passroutebackend.selfintro.repository.SelfIntroRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewStartTxServiceTest {

  @Mock private InterviewRoomRepository interviewRoomRepository;
  @Mock private InterviewSessionRepository interviewSessionRepository;
  @Mock private InterviewQuestionRepository interviewQuestionRepository;
  @Mock private RoomCrawlRequestRepository roomCrawlRequestRepository;
  @Mock private JobDescriptionRepository jobDescriptionRepository;
  @Mock private SelfIntroRepository selfIntroRepository;
  @Mock private DocumentRepository documentRepository;
  @Mock private DocumentAnalysisRepository documentAnalysisRepository;
  @Spy private ObjectMapper objectMapper = new ObjectMapper();
  @InjectMocks private InterviewStartTxService txService;

  @Nested
  @DisplayName("createPreparingSession 크롤링 데이터 연동")
  class CrawledDataIntegration {

    @Test
    @DisplayName("크롤링 완료 시 crawledData에 news와 techBlog가 포함된다")
    void includesCrawledDataWhenCompleted() {
      Long roomId = 1L;
      Long userId = 100L;
      InterviewRoom room = createRoom(roomId, userId);
      stubCommonDependencies(room);

      String payload = """
          {
            "news": [
              {"title": "뉴스 제목1", "url": "http://a.com", "description": "뉴스 내용1"},
              {"title": "뉴스 제목2", "url": "http://b.com", "description": "뉴스 내용2"}
            ],
            "tech_articles": [
              {"title": "블로그 제목1", "url": "http://c.com", "content": "블로그 본문1"}
            ],
            "summary": {"news_count": 2, "tech_article_count": 1}
          }
          """;
      RoomCrawlRequest crawlRequest = new RoomCrawlRequest(roomId, CrawlRequestStatus.PENDING);
      crawlRequest.markCompleted(payload);
      when(roomCrawlRequestRepository.findByRoomId(roomId)).thenReturn(Optional.of(crawlRequest));
      stubNoJd();

      SessionPreparation prep = txService.createPreparingSession(roomId, userId);
      CrawledData crawledData = prep.getAiRequest().getCrawledData();

      assertThat(crawledData).isNotNull();
      assertThat(crawledData.getNews()).contains("뉴스 제목1", "뉴스 내용1", "뉴스 제목2", "뉴스 내용2");
      assertThat(crawledData.getTechBlog()).contains("블로그 제목1", "블로그 본문1");
      assertThat(crawledData.getJd()).isNull();
    }

    @Test
    @DisplayName("크롤링 요청이 없고 JD도 없으면 crawledData는 null이다")
    void crawledDataIsNullWhenNoCrawlRequestAndNoJd() {
      Long roomId = 1L;
      Long userId = 100L;
      InterviewRoom room = createRoom(roomId, userId);
      stubCommonDependencies(room);
      when(roomCrawlRequestRepository.findByRoomId(roomId)).thenReturn(Optional.empty());
      stubNoJd();

      SessionPreparation prep = txService.createPreparingSession(roomId, userId);

      assertThat(prep.getAiRequest().getCrawledData()).isNull();
    }

    @Test
    @DisplayName("크롤링 상태가 COMPLETED가 아니면 뉴스/블로그는 포함되지 않는다")
    void crawledDataExcludesNewsWhenStatusNotCompleted() {
      Long roomId = 1L;
      Long userId = 100L;
      InterviewRoom room = createRoom(roomId, userId);
      stubCommonDependencies(room);

      RoomCrawlRequest crawlRequest = new RoomCrawlRequest(roomId, CrawlRequestStatus.IN_PROGRESS);
      when(roomCrawlRequestRepository.findByRoomId(roomId)).thenReturn(Optional.of(crawlRequest));
      stubNoJd();

      SessionPreparation prep = txService.createPreparingSession(roomId, userId);

      assertThat(prep.getAiRequest().getCrawledData()).isNull();
    }

    @Test
    @DisplayName("resultPayload JSON이 잘못되면 뉴스/블로그는 포함되지 않는다")
    void crawledDataExcludesNewsWhenPayloadMalformed() {
      Long roomId = 1L;
      Long userId = 100L;
      InterviewRoom room = createRoom(roomId, userId);
      stubCommonDependencies(room);

      RoomCrawlRequest crawlRequest = new RoomCrawlRequest(roomId, CrawlRequestStatus.PENDING);
      crawlRequest.markCompleted("{invalid json");
      when(roomCrawlRequestRepository.findByRoomId(roomId)).thenReturn(Optional.of(crawlRequest));
      stubNoJd();

      SessionPreparation prep = txService.createPreparingSession(roomId, userId);

      assertThat(prep.getAiRequest().getCrawledData()).isNull();
    }

    @Test
    @DisplayName("JD 데이터가 있으면 crawledData.jd에 포함된다")
    void includesJdWhenJobDescriptionExists() {
      Long roomId = 1L;
      Long userId = 100L;
      InterviewRoom room = createRoom(roomId, userId);
      stubCommonDependencies(room);
      when(roomCrawlRequestRepository.findByRoomId(roomId)).thenReturn(Optional.empty());

      JobDescription jd = createJobDescription("백엔드 개발자 채용", "Java/Spring 경력 3년 이상");
      when(jobDescriptionRepository.findByCompanyNameNormAndSourceIn(
          eq("테스트기업"), any())).thenReturn(List.of(jd));

      SessionPreparation prep = txService.createPreparingSession(roomId, userId);
      CrawledData crawledData = prep.getAiRequest().getCrawledData();

      assertThat(crawledData).isNotNull();
      assertThat(crawledData.getJd()).contains("백엔드 개발자 채용", "Java/Spring 경력 3년 이상");
      assertThat(crawledData.getNews()).isNull();
      assertThat(crawledData.getTechBlog()).isNull();
    }

    @Test
    @DisplayName("뉴스/블로그와 JD가 모두 있으면 crawledData에 전부 포함된다")
    void includesAllDataWhenBothExist() {
      Long roomId = 1L;
      Long userId = 100L;
      InterviewRoom room = createRoom(roomId, userId);
      stubCommonDependencies(room);

      String payload = """
          {
            "news": [{"title": "뉴스", "description": "내용"}],
            "tech_articles": [{"title": "블로그", "content": "본문"}]
          }
          """;
      RoomCrawlRequest crawlRequest = new RoomCrawlRequest(roomId, CrawlRequestStatus.PENDING);
      crawlRequest.markCompleted(payload);
      when(roomCrawlRequestRepository.findByRoomId(roomId)).thenReturn(Optional.of(crawlRequest));

      JobDescription jd = createJobDescription("채용공고", "상세 내용");
      when(jobDescriptionRepository.findByCompanyNameNormAndSourceIn(
          eq("테스트기업"), any())).thenReturn(List.of(jd));

      SessionPreparation prep = txService.createPreparingSession(roomId, userId);
      CrawledData crawledData = prep.getAiRequest().getCrawledData();

      assertThat(crawledData).isNotNull();
      assertThat(crawledData.getNews()).isNotNull();
      assertThat(crawledData.getTechBlog()).isNotNull();
      assertThat(crawledData.getJd()).contains("채용공고", "상세 내용");
    }

    private InterviewRoom createRoom(Long roomId, Long userId) {
      InterviewRoom room = InterviewRoom.builder()
          .userId(userId)
          .companyName("테스트기업")
          .jobPosition("백엔드")
          .interviewType(InterviewType.TECHNICAL)
          .interviewFormat(InterviewFormat.ONE_ON_ONE)
          .interviewMode("PRACTICE")
          .interviewCount(5)
          .difficulty(Difficulty.NORMAL)
          .pressureLevel(50)
          .followupCount(2)
          .status(RoomStatus.READY)
          .build();
      ReflectionTestUtils.setField(room, "id", roomId);
      return room;
    }

    private JobDescription createJobDescription(String title, String document) {
      try {
        var constructor = JobDescription.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        JobDescription jd = constructor.newInstance();
        ReflectionTestUtils.setField(jd, "url", "http://example.com/jd");
        ReflectionTestUtils.setField(jd, "title", title);
        ReflectionTestUtils.setField(jd, "document", document);
        return jd;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    private void stubCommonDependencies(InterviewRoom room) {
      Long roomId = room.getId();
      when(interviewRoomRepository.findByIdForUpdate(roomId)).thenReturn(Optional.of(room));
      when(interviewSessionRepository.findByInterviewRoomAndStatusIn(any(), any()))
          .thenReturn(Optional.empty());
      when(interviewSessionRepository.countByInterviewRoom(room)).thenReturn(0);
      when(interviewSessionRepository.save(any(InterviewSession.class)))
          .thenAnswer(invocation -> {
            InterviewSession session = invocation.getArgument(0);
            ReflectionTestUtils.setField(session, "id", 10L);
            return session;
          });
    }

    private void stubNoJd() {
      when(jobDescriptionRepository.findByCompanyNameNormAndSourceIn(any(), any()))
          .thenReturn(List.of());
    }
  }
}
