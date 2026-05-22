package passroutebackend.global.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockSmsService implements SmsService {

    @Override
    public void sendSms(String to, String message) {
        log.info("[MockSMS] to={} message={}", to, message);
    }
}
