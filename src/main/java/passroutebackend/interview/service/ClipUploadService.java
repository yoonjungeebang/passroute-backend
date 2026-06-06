package passroutebackend.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import passroutebackend.interview.dto.ClipUploadUrlResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClipUploadService {

  private final S3Presigner s3Presigner;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${cloud.aws.region.static}")
  private String region;

  private static final Duration PRESIGNED_URL_TTL = Duration.ofMinutes(10);

  public ClipUploadUrlResponse generatePresignedUrl(Long sessionId, Long questionId) {
    String key = String.format("clips/%d/%d/%s.webm", sessionId, questionId, UUID.randomUUID());

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType("video/webm")
        .build();

    PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(r -> r
        .signatureDuration(PRESIGNED_URL_TTL)
        .putObjectRequest(putObjectRequest)
    );

    String uploadUrl = presigned.url().toString();
    String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);

    return new ClipUploadUrlResponse(uploadUrl, fileUrl);
  }
}
