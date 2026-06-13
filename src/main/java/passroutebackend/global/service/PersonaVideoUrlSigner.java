package passroutebackend.global.service;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import passroutebackend.global.property.PersonaVideoProperties;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class PersonaVideoUrlSigner {

  private static final String S3_HOST_MARKER = ".s3.";

  private final ObjectProvider<S3Presigner> s3PresignerProvider;
  private final PersonaVideoProperties properties;

  public String sign(String videoUrl) {
    if (videoUrl == null || videoUrl.isBlank()) {
      return null;
    }
    if (!properties.isPresignEnabled()) {
      return videoUrl;
    }

    URI uri = URI.create(videoUrl);
    String host = uri.getHost();
    if (host == null || !host.contains(S3_HOST_MARKER)) {
      return videoUrl;
    }
    if (uri.getRawQuery() != null && uri.getRawQuery().contains("X-Amz-Signature=")) {
      return videoUrl;
    }

    S3Presigner s3Presigner = s3PresignerProvider.getIfAvailable();
    if (s3Presigner == null) {
      throw new IllegalStateException("S3 Presigner가 설정되지 않았습니다.");
    }

    String bucket = host.substring(0, host.indexOf(S3_HOST_MARKER));
    String path = uri.getPath();
    if (bucket.isBlank() || path == null || path.length() <= 1) {
      throw new IllegalArgumentException("올바르지 않은 S3 영상 URL입니다: " + videoUrl);
    }

    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(path.substring(1))
        .build();
    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(properties.getUrlExpiration())
        .getObjectRequest(getObjectRequest)
        .build();

    return s3Presigner.presignGetObject(presignRequest).url().toString();
  }
}
