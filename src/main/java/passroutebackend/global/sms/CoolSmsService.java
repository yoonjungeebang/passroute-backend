package passroutebackend.global.sms;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"dev", "prod"})
public class CoolSmsService implements SmsService {

    @Value("${sms.coolsms.api-key}")
    private String apiKey;

    @Value("${sms.coolsms.api-secret}")
    private String apiSecret;

    @Value("${sms.coolsms.sender}")
    private String sender;

    private DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    @Override
    public void sendSms(String to, String message) {
        Message sms = new Message();
        sms.setFrom(sender);
        sms.setTo(to);
        sms.setText(message);

        try {
            messageService.sendOne(new SingleMessageSendingRequest(sms));
            log.info("[CoolSMS] 발송 성공: to={}", to);
        } catch (Exception e) {
            log.error("[CoolSMS] 발송 실패: to={}, error={}", to, e.getMessage(), e);
            throw new RuntimeException("SMS 발송에 실패했습니다.", e);
        }
    }
}
