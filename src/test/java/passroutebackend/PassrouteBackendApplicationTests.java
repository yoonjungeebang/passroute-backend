package passroutebackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest(classes = PassrouteBackendApplicationTests.class)
class PassrouteBackendApplicationTests {

  @Test
  void contextLoads() {
  }

  // 아무 테스트 파일에 임시로 추가
  @Test
  void generatePassword() {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    String encoded = encoder.encode("Test1234!");
    System.out.println("==========================");
    System.out.println(encoded);
    System.out.println("==========================");
  }

}
