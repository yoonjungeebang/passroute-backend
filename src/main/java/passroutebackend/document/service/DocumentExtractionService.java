package passroutebackend.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import passroutebackend.document.entity.Document;
import passroutebackend.document.entity.Document.EmbedStatus;
import passroutebackend.document.entity.DocumentAnalysis;
import passroutebackend.document.repository.DocumentAnalysisRepository;
import passroutebackend.document.repository.DocumentRepository;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentExtractionService {

    private final S3Client s3Client;
    private final DocumentRepository documentRepository;
    private final DocumentAnalysisRepository documentAnalysisRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Async("documentExtractorExecutor")
    @Transactional
    public void extractAsync(Long documentId, String s3Key) {
        log.info("PDF 텍스트 추출 시작 documentId={}", documentId);

        Document document = documentRepository.findById(documentId).orElse(null);
        if (document == null) {
            log.warn("문서를 찾을 수 없음 documentId={}", documentId);
            return;
        }

        GetObjectRequest request = GetObjectRequest.builder()
            .bucket(bucket)
            .key(s3Key)
            .build();

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(request);
             PDDocument pdDocument = PDDocument.load(s3Object)) {

            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(pdDocument);

            documentAnalysisRepository.save(
                DocumentAnalysis.builder()
                    .document(document)
                    .extractedText(extractedText)
                    .analyzedAt(LocalDateTime.now())
                    .build()
            );

            // @Transactional 더티체킹으로 자동 반영 (명시적 save 불필요)
            document.updateEmbedStatus(EmbedStatus.DONE);
            log.info("PDF 텍스트 추출 완료 documentId={}, 길이={}", documentId, extractedText.length());

        } catch (Exception e) {
            log.error("PDF 텍스트 추출 실패 documentId={}", documentId, e);
            document.updateEmbedStatus(EmbedStatus.FAILED);
        }
    }
}
