package passroutebackend.global.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import passroutebackend.global.property.PersonaVideoProperties;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class PersonaVideoUrlSignerTest {

  @Mock private S3Presigner presigner;
  @Mock private ObjectProvider<S3Presigner> presignerProvider;

  private PersonaVideoUrlSigner signer;

  @BeforeEach
  void setUp() {
    PersonaVideoProperties properties = new PersonaVideoProperties();
    properties.setPresignEnabled(true);
    properties.setUrlExpiration(Duration.ofHours(6));
    signer = new PersonaVideoUrlSigner(presignerProvider, properties);
  }

  @Test
  void signsS3ObjectUrl() throws Exception {
    PresignedGetObjectRequest signedRequest = org.mockito.Mockito.mock(
        PresignedGetObjectRequest.class);
    when(signedRequest.url()).thenReturn(
        URI.create("https://signed.example/video.mp4?X-Amz-Signature=test").toURL());
    when(presignerProvider.getIfAvailable()).thenReturn(presigner);
    when(presigner.presignGetObject(any(GetObjectPresignRequest.class)))
        .thenReturn(signedRequest);

    String result = signer.sign(
        "https://passroute-files.s3.ap-northeast-2.amazonaws.com/avatars/test.mp4");

    assertThat(result).isEqualTo(
        "https://signed.example/video.mp4?X-Amz-Signature=test");
  }

  @Test
  void keepsNonS3UrlUnchanged() {
    String url = "https://cdn.example.com/video.mp4";

    assertThat(signer.sign(url)).isEqualTo(url);
  }
}
